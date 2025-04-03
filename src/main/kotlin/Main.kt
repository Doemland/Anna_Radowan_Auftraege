import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.orderprocessor.OrderProcessorApp
import javax.swing.Painter

@Composable
@Preview
fun App() {
    var text by remember { mutableStateOf("Hello, World!",) }
    MaterialTheme {
        Column {
            Text("Anna Radowan KG")

            Button(onClick = {
                text = "Hello, Desktop!"
            }) {
                Text(text)
            }
            OrderProcessorApp()
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
