package com.sotospeak.app.screens.showcase.sections

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.sotospeak.app.components.MergeProgressDialog
import com.sotospeak.app.screens.showcase.DemoCard
import com.sotospeak.app.screens.showcase.ShowcaseScaffold
import com.sotospeak.app.screens.showcase.ShowcaseSection

/**
 * Демо «Диалоги»: MergeProgressDialog (гостевой прогресс) и стоковый
 * M3 AlertDialog. Кнопки открывают по одному диалогу.
 */
@Composable
fun DialogsSection(onBack: () -> Unit) {
    var showMerge by remember { mutableStateOf(false) }
    var showAlert by remember { mutableStateOf(false) }

    ShowcaseScaffold(section = ShowcaseSection.Dialogs, onBack = onBack) {
        DemoCard(title = "MergeProgressDialog") {
            Button(onClick = { showMerge = true }) {
                Text("Открыть")
            }
        }
        DemoCard(title = "M3 AlertDialog") {
            Button(onClick = { showAlert = true }) {
                Text("Открыть")
            }
        }
    }

    if (showMerge) {
        MergeProgressDialog(
            error = null,
            onMerge = { showMerge = false },
            onDismiss = { showMerge = false }
        )
    }
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Пример диалога") },
            text = { Text("Стоковый M3 AlertDialog с бренд-темой FunnyTheme.") },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
