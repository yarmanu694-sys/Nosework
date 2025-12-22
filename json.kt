package utils

import android.content.Context
import com.example.my.models.competitions
import com.example.my.models.pr
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object JsonManager {
    private val gson = Gson()

    /**
     * Загружает конфигурацию соревнования из файла в папке assets.
     * @param filename Имя файла (например, "competition.json").
     * @param context Контекст приложения.
     * @return Объект Competition или null в случае ошибки.
     */
    fun loadCompetitionFromAssets(filename: String, context: Context): competitions? {
        return try {
            val inputStream = context.assets.open(filename)
            val reader = inputStream.bufferedReader()
            val json = reader.readText()
            reader.close()
            inputStream.close()

            val type = object : TypeToken<competitions>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Сохраняет результат участника в JSON-файл.
     * @param result Объект ParticipantResult для сохранения.
     * @param filepath Полный путь к файлу для сохранения.
     */
    fun saveResultToFile(result: pr, filepath: String) {
        try {
            val writer = FileWriter(filepath)
            gson.toJson(result, writer)
            writer.close()
            println("Результат сохранен в: $filepath")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Загружает результат участника из JSON-файла.
     * @param filepath Полный путь к файлу для загрузки.
     * @return Объект ParticipantResult или null в случае ошибки.
     */
    fun loadParticipantResultFromFile(filepath: String): pr? {
        return try {
            val reader = FileReader(filepath)
            val type = object : TypeToken<pr>() {}.type
            val result = gson.fromJson<>()son(reader,type)

            reader.close()
            result
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}