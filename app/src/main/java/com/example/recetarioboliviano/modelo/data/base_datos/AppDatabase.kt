package com.example.recetarioboliviano.modelo.data.base_datos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.recetarioboliviano.modelo.dao.CarpetaDao
import com.example.recetarioboliviano.modelo.dao.RecetaDao
import com.example.recetarioboliviano.modelo.dao.UsuarioDao
import com.example.recetarioboliviano.modelo.entidades.Carpeta
import com.example.recetarioboliviano.modelo.entidades.Receta
import com.example.recetarioboliviano.modelo.entidades.RecetaCarpetaCrossRef
import com.example.recetarioboliviano.modelo.entidades.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Base de datos de la aplicación Recetario Boliviano.
 */
@Database(
    entities = [Usuario::class, Receta::class, Carpeta::class, RecetaCarpetaCrossRef::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun recetaDao(): RecetaDao
    abstract fun carpetaDao(): CarpetaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recetario_boliviano_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                poblarRecetasPredefinidas(database.recetaDao())
                                // No creamos el usuario aquí para obligar al registro manual
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun poblarRecetasPredefinidas(recetaDao: RecetaDao) {
            val recetasPredefinidas = listOf(
                Receta(
                    titulo = "Chairo Paceño",
                    tiempoPreparacion = "1h 30m",
                    cantidadPersonas = "4 personas",
                    ingredientes = "- 500g chuño negro\n- 4 papas medianas\n- 250g mote cocido\n- 300g carne de cordero\n- 1 cebolla\n- Hierbabuena\n- Sal al gusto\n- Ají colorado",
                    preparacion = "1. Remojar el chuño durante toda la noche.\n2. Hervir el chuño martajado con agua.\n3. Agregar las papas y el mote.\n4. Incorporar la carne de cordero hervida.\n5. Sazonar con cebolla, hierbabuena y ají.\n6. Cocinar a fuego lento por 1 hora.\n7. Servir caliente.",
                    categoria = "Sopa",
                    departamento = "La Paz",
                    imagenUri = "img_chairo" // 👈 CORREGIDO: Nombre puro del drawable
                ),
                Receta(
                    titulo = "Sopa de Maní",
                    tiempoPreparacion = "1h 15m",
                    cantidadPersonas = "6 personas",
                    ingredientes = "- 1 taza maní molido\n- 500g carne (res o pollo)\n- 4 papas\n- 2 zanahorias\n- 1 cebolla\n- 2 dientes de ajo\n- 1 pimentón\n- Sal, comino y pimienta",
                    preparacion = "1. Hervir la carne con agua, cebolla y ajo.\n2. Agregar las papas y zanahorias cortadas.\n3. Incorporar el maní molido diluido.\n4. Añadir el pimentón y especias.\n5. Cocinar por 30 minutos.\n6. Rectificar sazón.\n7. Servir caliente con arroz.",
                    categoria = "Sopa",
                    departamento = "Cochabamba",
                    imagenUri = "img_sopa_mani" // 👈 CORREGIDO
                ),
                Receta(
                    titulo = "Silpancho Cochabambino",
                    tiempoPreparacion = "45 minutos",
                    cantidadPersonas = "4 personas",
                    ingredientes = "- 4 bistés de carne\n- 2 tazas arroz\n- 4 papas grandes\n- 4 huevos fritos\n- 1 taza pan molido\n- 1 cebolla\n- Ají amarillo\n- Sal y pimienta",
                    preparacion = "1. Aplanar los bistés muy delgados.\n2. Sazonar con sal, pimienta y ajo.\n3. Empanizar con pan molido.\n4. Freír en aceite caliente hasta dorar.\n5. Preparar arroz graneado.\n6. Freír las papas en bastones.\n7. Freír los huevos.\n8. Servir con arroz, papas y huevo.",
                    categoria = "Segundo",
                    departamento = "Cochabamba",
                    imagenUri = "img_silpancho" // 👈 CORREGIDO
                ),
                Receta(
                    titulo = "Majadito",
                    tiempoPreparacion = "1h 15m",
                    cantidadPersonas = "4 personas",
                    ingredientes = "- 300g charque (carne deshidratada)\n- 2 tazas arroz\n- 4 huevos fritos\n- 2 plátanos fritos\n- 1 cebolla\n- 2 dientes de ajo\n- Ají molido\n- Sal",
                    preparacion = "1. Remojar el charque para desalar.\n2. Hervir el arroz con agua.\n3. Freír el charque hasta dorar.\n4. Sofréir cebolla y ajo.\n5. Mezclar arroz con el charque.\n6. Freír los huevos y plátanos.\n7. Servir con arroz, huevo y plátano.",
                    categoria = "Segundo",
                    departamento = "Santa Cruz",
                    imagenUri = "img_majadito" // 👈 CORREGIDO
                ),
                Receta(
                    titulo = "Pique Macho",
                    tiempoPreparacion = "1 hora",
                    cantidadPersonas = "4 personas",
                    ingredientes = "- 400g carne de res\n- 4 salchichas\n- 4 papas grandes\n- 1 cebolla\n- 2 tomates\n- 1 locoto\n- Ají amarillo\n- Sal y pimienta",
                    preparacion = "1. Cortar la carne en cubos y freír.\n2. Freír las salchichas en rodajas.\n3. Cortar las papas en bastones y freír.\n4. Sofreír la cebolla, tomate y locoto.\n5. Mezclar todo en un sartén grande.\n6. Sazonar con ají y especias.\n7. Servir caliente.",
                    categoria = "Segundo",
                    departamento = "Cochabamba",
                    imagenUri = "img_pique_macho" // 👈 CORREGIDO
                ),
                Receta(
                    titulo = "Salteña Boliviana",
                    tiempoPreparacion = "3 horas",
                    cantidadPersonas = "12 unidades",
                    ingredientes = "- 500g harina\n- 250g carne de res\n- 2 papas\n- 1 cebolla\n- Ají colorado\n- Gelatina sin sabor\n- Aceitunas y pasas",
                    preparacion = "1. Preparar el jigote (relleno) el día anterior.\n2. Cocinar la carne con cebolla, ají y especias.\n3. Agregar papa cocida y gelatina.\n4. Enfriar hasta que cuaje.\n5. Preparar la masa con harina y manteca.\n6. Rellenar y repulgar.\n7. Hornear a fuego alto por 15 minutos.",
                    categoria = "Segundo",
                    departamento = "Bolivia",
                    imagenUri = "img_saltena" // 👈 CORREGIDO
                )
            )
            recetaDao.insertarVarias(recetasPredefinidas)
        }
    }
}