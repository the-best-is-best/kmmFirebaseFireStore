package io.gituhb.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.firebase_firestore.KFirebaseFirestore
import io.gituhb.demo.theme.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
internal fun App() = AppTheme {
    val kFirebaseFirestore = KFirebaseFirestore()
    val scope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton({
            scope.launch {
                kFirebaseFirestore.addDocument(
                    "test",
                    "test", mapOf("name" to "test", "age" to 10)
                )
                kFirebaseFirestore.addDocument(
                    "test",
                    "test2", mapOf("name" to "tes2t", "age" to 20)
                )
            }
        }) {
            Text("Add Document")
        }
        ElevatedButton({
            scope.launch {
                val res = kFirebaseFirestore.getDocuments("test")
                res.fold(
                    onSuccess = { documents ->
                        println("Documents: $documents")
                    },
                    onFailure = { exception ->
                        println("Error getting documents: $exception")
                    }
                )
            }
        }) {
            Text("Get Documents")
        }

        ElevatedButton({
            scope.launch {
                val res = kFirebaseFirestore.getDocumentById("test", "test")
                res.fold(
                    onSuccess = { document ->
                        println("Document: $document")
                    },
                    onFailure = { exception ->
                        println("Error getting document: $exception")
                    }
                )
            }
        }) {
            Text("Get Document By Id")
        }

        ElevatedButton({
            scope.launch {
                val res = kFirebaseFirestore.queryDocuments(
                    "test",
                    filters = listOf(
                        mapOf("field" to "name", "operator" to "==", "value" to "test")
                    ),
                    orderBy = null,
                    limit = null
                )
                res.fold(
                    onSuccess = { documents ->
                        println("Documents: $documents")
                    },
                    onFailure = { exception ->
                        println("Error getting documents: $exception")
                    }
                )
            }
        }) {
            Text("Query Documents")
        }

        ElevatedButton({
            scope.launch {
                kFirebaseFirestore.updateDocument(
                    "test",
                    "test",
                    mapOf("name" to "test updated", "age" to 30)
                )
            }
        }) {
            Text("Update Document")
        }

        ElevatedButton({
            scope.launch {
                kFirebaseFirestore.deleteDocument("test", "test")
            }
        }) {
            Text("Delete Document")
        }

        ElevatedButton({
            scope.launch {
                kFirebaseFirestore.listenToCollection("test")
                    .collect { result ->
                        // Handle success or failure
                        if (result.isSuccess) {
                            println("data : ${result.getOrNull()}")
                        } else {
                            // Handle failure (show error message)
                            println("Error: ${result.exceptionOrNull()?.message}")
                        }
                    }

            }
        }) {
            Text("Listen To Collection")
        }

    }
}
