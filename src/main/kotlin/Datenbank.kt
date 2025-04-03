package com.example.orderprocessor



import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrderProcessorApp()
        }
        // Initialisiere die Datenbank
        val dbHelper = ProductDatabaseHelper(this)
        dbHelper.loadProductsFromCsv("/mnt/data/Datenexport-RADOWAN-PC1.csv")
    }
}

@Composable
fun OrderProcessorApp() {
    var orders by remember { mutableStateOf(loadOrders()) }
    val dbHelper = ProductDatabaseHelper(LocalContext.current)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Bestellungen bearbeiten", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(orders.size) { index ->
                val matchedProduct = dbHelper.findMatchingProduct(orders[index].details)
                OrderEditor(order = orders[index], matchedProduct) { updatedOrder ->
                    orders = orders.toMutableList().apply { this[index] = updatedOrder }
                }
            }
        }
        Button(onClick = { exportOrdersToCsv(orders) }) {
            Text("Export als CSV")
        }
    }
}

@Composable
fun OrderEditor(order: Order, matchedProduct: Product?, onOrderChange: (Order) -> Unit) {
    var text by remember { mutableStateOf(order.details) }

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        BasicTextField(value = text, onValueChange = {
            text = it
            onOrderChange(order.copy(details = it))
        })
        matchedProduct?.let {
            Text("Gefundenes Produkt: ${it.name} (${it.number})")
        }
        Divider()
    }
}

data class Order(val details: String)

data class Product(val number: String, val name: String, val unit: String?, val supplier: String?)

class ProductDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY, number TEXT, name TEXT, unit TEXT, supplier TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS products")
        onCreate(db)
    }

    fun loadProductsFromCsv(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return
        val db = writableDatabase
        file.readLines().drop(1).forEach { line ->
            val values = line.split(";")
            if (values.size > 4) {
                val number = values[0]
                val name = values[1]
                val unit = values[3]
                val supplier = values[8]
                db.execSQL("INSERT INTO products (number, name, unit, supplier) VALUES (?, ?, ?, ?)", arrayOf(number, name, unit, supplier))
            }
        }
    }

    fun findMatchingProduct(orderDetails: String): Product? {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT number, name, unit, supplier FROM products WHERE name LIKE ? LIMIT 1", arrayOf("%$orderDetails%"))
        return if (cursor.moveToFirst()) {
            Product(
                number = cursor.getString(0),
                name = cursor.getString(1),
                unit = cursor.getString(2),
                supplier = cursor.getString(3)
            )
        } else {
            null
        }.also {
            cursor.close()
        }
    }
}

fun loadOrders(): List<Order> {
    // Implementiere das Einlesen aus Excel, PDF, E-Mail usw.
    return listOf(Order("Bestellung 1"), Order("Bestellung 2"))
}

fun exportOrdersToCsv(orders: List<Order>) {
    val file = File("/storage/emulated/0/Download/orders.csv")
    file.writeText(orders.joinToString("\n") { it.details })
}

private const val DATABASE_NAME = "products.db"
private const val DATABASE_VERSION = 1
