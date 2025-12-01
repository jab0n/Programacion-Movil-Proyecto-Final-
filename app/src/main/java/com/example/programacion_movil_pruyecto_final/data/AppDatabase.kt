package com.example.programacion_movil_pruyecto_final.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Anotación que define la base de datos de Room.
// entities: Especifica las clases de entidad que pertenecen a esta base de datos.
// version: Especifica la versión de la base de datos. Debe incrementarse al cambiar el esquema.
// exportSchema: Si se debe exportar el esquema de la base de datos a un archivo JSON. Es útil para el control de versiones del esquema.
@Database(entities = [Note::class, Task::class, Attachment::class, Reminder::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Métodos abstractos para obtener los DAO (Data Access Objects).
    // Room generará la implementación de estos métodos.
    abstract fun noteDao(): NoteDao
    abstract fun taskDao(): TaskDao

    // Companion object para permitir el acceso a los métodos sin instanciar la clase.
    companion object {
        // La anotación @Volatile asegura que el valor de INSTANCE sea siempre el más actualizado
        // y visible para todos los hilos de ejecución. Es útil para evitar problemas de concurrencia.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Método para obtener la instancia de la base de datos. Implementa el patrón Singleton.
        fun getDatabase(context: Context): AppDatabase {
            // Si la instancia no es nula, la devuelve. Si es nula, crea la base de datos.
            return INSTANCE ?: synchronized(this) {
                // El bloque synchronized asegura que solo un hilo a la vez pueda ejecutar este código,
                // evitando que se creen múltiples instancias de la base de datos.
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes_database" // Nombre del archivo de la base de datos.
                )
                // fallbackToDestructiveMigration() permite a Room recrear las tablas de la base de datos
                // si no se proporcionan estrategias de migración. Esto borrará todos los datos existentes.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                // Devuelve la instancia recién creada.
                instance
            }
        }
    }
}
