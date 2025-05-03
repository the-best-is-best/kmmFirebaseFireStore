package io.github.firebase_firestore

import io.github.native.kfirebase.firestore.FIRCollectionReference
import io.github.native.kfirebase.firestore.FIRDocumentReference
import io.github.native.kfirebase.firestore.FIRDocumentSnapshot
import io.github.native.kfirebase.firestore.FIRFilter
import io.github.native.kfirebase.firestore.FIRFirestore
import io.github.native.kfirebase.firestore.FIRQuery
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSNumber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
actual class KFirebaseFirestore {

    private val firestore = FIRFirestore.firestore()

    actual suspend fun addDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any?>
    ): Result<Boolean> = suspendCancellableCoroutine { cont ->
        val collectionRef: FIRCollectionReference = firestore.collectionWithPath(collection)
        val documentRef: FIRDocumentReference = collectionRef.documentWithPath(documentId)

        // Use the addDocs (or the appropriate function for adding a document)
        documentRef.setData(convertStringMapToAnyMap(data)) { callbackIos ->

            // Check for errors in the callback
            val error = callbackIos?.convertNSErrorToException()
            if (error != null) {
                // If there's an error, resume with a failure result
                cont.resumeWith(Result.failure(error))
            } else {
                // If successful, resume with success
                cont.resume(Result.success(true))
            }
        }
    }


    // This function assumes Firestore documents are represented as Maps on iOS.
    actual suspend fun getDocuments(collection: String): Result<List<Map<String, Any?>>> =
        suspendCancellableCoroutine { cont ->
            val collectionRef: FIRCollectionReference = firestore.collectionWithPath(collection)

            // Fetch documents from Firestore
            collectionRef.getDocumentsWithCompletion { callbackIos, error ->

                if (error != null) {
                    // If there's an error, resume with a failure result
                    cont.resumeWith(Result.failure(error.convertNSErrorToException()))
                } else {
                    // If successful, convert documents to a list of maps
                    try {
                        val documents = callbackIos?.documents()

                        // Convert the documents to a List<Map<String, Any?>>
                        val documentMaps = documents?.mapNotNull { document ->
                            // Extract data from each document and convert it to a map
                            (document as? FIRDocumentSnapshot)?.data()
                        }

                        // Resume with success and return the list of maps
                        cont.resume(Result.success(convertToListOfMaps(documentMaps)))
                    } catch (exception: Exception) {
                        // Handle potential conversion errors
                        cont.resumeWithException(exception)
                    }
                }
            }
        }

    actual suspend fun getDocumentById(
        collection: String,
        documentId: String
    ): Result<Map<String, Any?>> = suspendCancellableCoroutine { cont ->

        // Get Firestore instance
        val documentRef = firestore.collectionWithPath(collection).documentWithPath(documentId)

        // Fetch the document by ID
        documentRef.getDocumentWithCompletion { callbackIos, error ->

            // Check for errors in the callback
            val data = callbackIos?.data()

            if (error != null) {
                // If there's an error, resume with a failure result
                cont.resumeWith(Result.failure(error.convertNSErrorToException()))
            } else {
                // If successful, resume with the converted document data
                cont.resume(Result.success(convertAnyMapToStringMap(data)))
            }
        }
    }


    actual fun listenToCollection(
        collection: String,
    ): Flow<Result<List<Map<String, Any?>>>> = callbackFlow {
        val collectionRef = firestore.collectionWithPath(collection)

        // Set up a listener on the collection
        val listener = collectionRef.addSnapshotListener { snapshot, error ->
            println("🔥 iOS listener triggered") // Debug log

            if (error != null) {
                println("❌ Firestore error: ${error.localizedDescription}") // Debug log
                trySend(Result.failure(error.convertNSErrorToException())).isSuccess
                return@addSnapshotListener
            }

            val data = snapshot?.documents?.map { doc ->
                (doc as? FIRDocumentSnapshot)?.data()?.toMap() ?: emptyMap<String, Any?>()
            } ?: emptyList()

            println("📄 Documents: $data") // Debug log
            trySend(Result.success(convertToListOfMaps(data))).isSuccess
        }

        awaitClose {
            listener.remove()
            println("🧹 Listener removed")
        }
    }


    actual suspend fun queryDocuments(
        collection: String,
        filters: List<Map<String, Comparable<*>>>,
        orderBy: String?,
        limit: Long?
    ): Result<List<Map<String, Any?>>> = suspendCancellableCoroutine { cont ->
        var query: FIRQuery =
            firestore.collectionWithPath(collection) // Start with FIRQuery from the collection reference

        // Apply filters to the query
        var firQuery: FIRQuery? = query
        filters.forEach { filter ->
            val field = filter["field"] as? String ?: return@forEach
            val operator = filter["operator"] as? String ?: return@forEach
            val value = filter["value"] ?: return@forEach

            val firFilter = when (operator) {
                "==" -> FIRFilter.filterWhereField(field, isEqualTo = value)
                "!=" -> FIRFilter.filterWhereField(field, isNotEqualTo = value)
                ">" -> FIRFilter.filterWhereField(field, isGreaterThan = value)
                ">=" -> FIRFilter.filterWhereField(field, isGreaterThanOrEqualTo = value)
                "<" -> FIRFilter.filterWhereField(field, isLessThan = value)
                "<=" -> FIRFilter.filterWhereField(field, isLessThanOrEqualTo = value)
                "in" -> if (value is List<*>) FIRFilter.filterWhereField(
                    field,
                    `in` = value.filterIsInstance<Any>()
                ) else null

                "not-in" -> if (value is List<*>) FIRFilter.filterWhereField(
                    field,
                    notIn = value.filterIsInstance<Any>()
                ) else null

                "array-contains-any" -> if (value is List<*>) FIRFilter.filterWhereField(
                    field,
                    arrayContainsAny = value.filterIsInstance<Any>()
                ) else null

                "array-contains" -> FIRFilter.filterWhereField(field, arrayContains = value)
                else -> null
            }

            firFilter?.let {
                firQuery = firQuery?.queryWhereFilter(it)
            }
        }
        // Apply orderBy and limit to the query if provided
        if (orderBy != null) {
            query = query.queryOrderedByField(orderBy)
        }

        if (limit != null) {
            query = query.queryLimitedTo(limit)
        }

        // Execute the query and fetch results
        query.getDocumentsWithCompletion { callbackIos, error ->
            // Extract the documents from the callback
            val documents = callbackIos?.documents

            // Check if there's an error
            if (error != null) {
                cont.resumeWith(Result.failure(error.convertNSErrorToException()))
            } else {
                // Convert the documents to List<Map<String, Any?>> and return success result
                val convertedData = documents?.let { convertToListOfMaps(it) }
                cont.resume(Result.success(convertedData ?: emptyList()))
            }
        }
    }

    actual suspend fun updateDocument(
        collection: String,
        documentId: String,
        data: Map<String, Any?>
    ): Result<Boolean> = suspendCancellableCoroutine { cont ->
        val documentRef = firestore.collectionWithPath(collection).documentWithPath(documentId)

        // Use setData with merge option to update the document
        documentRef.setData(convertStringMapToAnyMap(data), merge = true) { callbackIos ->
            // Check for errors
            val error = callbackIos?.convertNSErrorToException()
            if (error != null) {
                // If there's an error, resume with exception
                cont.resumeWithException(error)
            } else {
                // If successful, resume with success
                cont.resume(Result.success(true))
            }
        }
    }

    actual suspend fun deleteDocument(
        collection: String,
        documentId: String
    ): Result<Unit> = suspendCancellableCoroutine { cont ->
        val documentRef = firestore.collectionWithPath(collection).documentWithPath(documentId)

        // Call the delete function on the document reference
        documentRef.deleteDocumentWithCompletion { error ->
            // Check for errors
            if (error != null) {
                // If there's an error, resume with failure result
                cont.resumeWith(Result.failure(error.convertNSErrorToException()))
            } else {
                // If successful, resume with success result
                cont.resume(Result.success(Unit))
            }
        }
    }

//    actual suspend fun batchWrite(
//        addOperations: List<Pair<String, Any>>,
//        updateOperations: List<Triple<String, String, Any>>,
//        deleteOperations: List<Pair<String, String>>
//    ): Result<Unit> = suspendCancellableCoroutine { cont ->
//        // Create a new Firestore batch reference
//        val batch = firestore.batch() // Assuming firestore.batch() works for creating a batch
//
//        // Handle the "add" operations (adding new documents)
//        addOperations.forEach { (collection, data) ->
//            // Assuming collection reference
//            val collectionRef = firestore.collectionWithPath(collection)
//
//            // Firestore will automatically generate a document ID for the "add" operation
//            val documentRef = collectionRef.documentWithAutoID() // This is the correct method for auto-generating document ID
//            batch.setData(convertToMapAny(data), documentRef) // Use the setData with the document reference
//        }
//
//        // Handle the "update" operations (updating existing documents)
//        updateOperations.forEach { (collection, documentId, data) ->
//            val documentRef = firestore.collectionWithPath(collection).documentWithPath(documentId)
//
//            // Check if data is already a Map, and cast it if necessary
//            val convertedData: Map<Any?, *> = when (data) {
//                is Map<*, *> -> data as Map<Any?, *>  // If it's already a Map, cast it
//                else -> throw IllegalArgumentException("Data should be a Map for update operation.")
//            }
//
//            batch.updateData(convertedData, documentRef) // Update the document with the provided data
//        }
//
//
//
//        // Handle the "delete" operations (deleting documents)
//        deleteOperations.forEach { (collection, documentId) ->
//            // Get the document reference
//            val documentRef = firestore.collectionWithPath(collection).documentWithPath(documentId)
//            batch.deleteDocument(documentRef) // Delete the document from the batch
//        }
//
//        // Commit the batch
//        batch.commitWithCompletion { error ->
//            if (error != null) {
//                // If an error occurred, resume with a failure
//                cont.resumeWith(Result.failure(error.convertNSErrorToException()))
//            } else {
//                // If successful, resume with success (no return value needed)
//                cont.resume(Result.success(Unit))
//            }
//        }
    // }


    // Helper functions
    private fun convertStringMapToAnyMap(input: Map<String, Any?>): Map<Any?, *> {
        return input.mapKeys { it.key } // Convert keys to Any?
            .mapValues { it.value } // Keep values as is
            .toMap() // Convert back to a Map
    }

    private fun convertToListOfMaps(input: List<*>?): List<Map<String, Any?>> {
        return input?.mapNotNull { it as? Map<String, Any?> } ?: emptyList()
    }


    private fun convertAnyMapToStringMap(input: Map<Any?, *>?): Map<String, Any?> {
        return input?.mapNotNull { (key, value) ->
            (key as? String)?.let { it to value }
        }?.toMap() ?: emptyMap()
    }

    private fun convertLongToNSNumber(longValue: Long?): NSNumber? {
        return longValue?.let { NSNumber(it.toDouble()) }
    }

    private fun convertToMapAny(data: Any): Map<Any?, *> {
        return when (data) {
            is Map<*, *> -> {
                // Convert any Map to Map<Any?, *>
                data.entries.associate { it.key to it.value }
            }

            else -> {
                // Wrap non-Map data in a map with a single key-value pair
                mapOf("data" to data)
            }
        }
    }

}
