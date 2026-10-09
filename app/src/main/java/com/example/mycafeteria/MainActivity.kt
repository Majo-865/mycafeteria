package com.example.mycafeteria

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}


val PrimaryOrange = Color(0xFFFAB314)
val BgPrimary = Color(0xFFFAE796)
val Accent = Color(0xFFFAE800)
val HellAccent = Color(0xFFFAAF4D)


// Color café oscuro que se usa en la tarjeta contenedora de la imagen
val DarkBrown = Color(0xFFC35425)

// Modelo de datos para representar cada producto del inventario
data class Producto(
    val id: Int,
    val nombre: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val categoria: String // "Casero", "Empacados" o "Bebidas"
) {
    val precioTotal: Double
        get() = precioUnitario * cantidad
}


@Composable
// Las variables en esta sección estan correctas. No cambiar.
fun App() {

    var pantallaActual by remember { mutableStateOf("login") }

    when (pantallaActual) {
        "login" -> LoginScreen(onEntrarClick = { pantallaActual = "menu" })
        "menu" -> MenuScreen(
            onInventarioClick = { pantallaActual = "inventario" },
            onVolverClick = { pantallaActual = "login" }
        )
        "inventario" -> InventarioScreen(
            onVolverClick = { pantallaActual = "menu" }
        )
    }
}


@Composable
fun LoginScreen(onEntrarClick: () -> Unit) {
    var nombre by remember { mutableStateOf("") }
    var contraseña by remember { mutableStateOf("") }
    var mensajeError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryOrange)
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "MyCafeteria",
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Ingresar",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        //---Campo 1: Usuario---
        Text(
            text = "Usuario",
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = {
                nombre = it
                mensajeError = false
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        //---Campo 2: Contraseña---
        Text(
            text = "Contraseña",
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = contraseña,
            onValueChange = {
                contraseña = it
                mensajeError = false
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (mensajeError) {
            Text(
                text = "Usuario o contraseña incorrectos, intente de nuevo",
                color = Color.Red,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(13.dp))
        }

        Button(
            onClick = {
                if (nombre == "admin" && contraseña == "1234") {
                    mensajeError = false
                    onEntrarClick()
                } else {
                    mensajeError = true
                }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent)
        ) {
            Text(
                text = "Entrar",
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}
//PANTALLA MENÚ PRINCIPAL

@Composable
fun MenuScreen(onVolverClick: () -> Unit,
               onInventarioClick: ()-> Unit ){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text(
            text = "MyCafeteria",
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryOrange
        )

        Spacer(modifier = Modifier.height(28.dp))

        //SUBTITULO DE MENÚ

        Text(
            text = "MENÚ",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = HellAccent
        )

        Spacer(modifier = Modifier.height(36.dp))


        //BOTÓN INVENTARIO
        Button(
            onClick = { }, // onInventarioCLick,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(55.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HellAccent)
        ) {
            Text(
                text = "INVENTARIO",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        //BOTON VENTAS
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(55.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HellAccent)
        ) {
            Text(
                text = "VENTAS",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(20.dp))


        // BOOTN DE CONTABILIDAD
        Button(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(55.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HellAccent)
        ) {
            Text(
                text = "Contabilidad",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        //BOTON PARA VOLVER A INICIO
        Button(
            onClick = {onVolverClick()},
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)

        ) {
            Text(
                text = "VOLVER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
            )


        }
    }

}


//ANIDACIÓN DE LA BASE DE DATOS CON ROOM, ERROR EN LA CLASE "PRODUCTO" TANTO AQUÍ
//COMO EN LAS DECLARACIONES DEL INICIO.
//ESTA SECCION DEL CÓDIGO ESTA INCOMPLETA, AUN FALTA AGREGAR EL RESTO DE LA ANIDACION DE BASE DE DATOS-

@Entity(tableName = "tabla_productos")
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val precioUnitario: Double,
    val cantidad: Int,
    val categoria: String
) {
    val precioTotal: Double
        get() = precioUnitario * cantidad
}

@Dao
interface ProductoDao {
    @Query("SELECT * FROM tabla_productos ORDER BY id DESC")
    fun obtenerProductos(): Flow<List<Producto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(producto: Producto)

    @Delete
    suspend fun eliminar(producto: Producto)
}

@Database(entities = [Producto::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productoDao(): ProductoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cafeteria_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class InventarioViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.productoDao()

    val listaProductos: StateFlow<List<Producto>> = dao.obtenerProductos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun guardarProducto(producto: Producto) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertarOActualizar(producto)
        }
    }

    fun eliminarProducto(producto: Producto) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.eliminar(producto)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)

//ESTA ES LA SECCION DE INVENTARIO UNA VEZ EL BOTÓN NOS DÉ INGRESO. FALTA HACER UNA REVISIÓN
//
@Composable
fun InventarioScreen(onVolverClick: () -> Unit) {


    //val listaProductos by viewModel.listaProductos.collectAsState()

    var productoSeleccionadoId by remember { mutableStateOf<Int?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var productoAEditar by remember { mutableStateOf<Producto?>(null) }

    var nombreInput by remember { mutableStateOf("") }
    var precioInput by remember { mutableStateOf("") }
    var cantidadInput by remember { mutableStateOf("") }
    var categoriaInput by remember { mutableStateOf("Casero") }
    var menuCategoriaExpandido by remember { mutableStateOf(false) }
    val opcionesCategoria = listOf("Casero", "Empacados", "Bebidas")

    fun abrirDialogo(producto: Producto? = null) {
        if (producto != null) {
            productoAEditar = producto
            nombreInput = producto.nombre
            precioInput = producto.precioUnitario.toString()
            cantidadInput = producto.cantidad.toString()
            categoriaInput = producto.categoria
        } else {
            productoAEditar = null
            nombreInput = ""
            precioInput = ""
            cantidadInput = ""
            categoriaInput = "Casero"
        }
        mostrarDialogo = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MyCafeteria",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = DarkBrown
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "INVENTARIO",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = HellAccent
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkBrown, shape = RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "PRODUCTO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1.2f)
                )
                Text(
                    text = "CANTIDAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(0.9f)
                )
                Text(
                    text = "PRECIO\nUNITARIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "PRECIO\nTOTAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}





@Preview
@Composable
fun Vista(){
    App()
}
