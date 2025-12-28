package com.example.my
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import adapters.ResultAdapter
import com.example.my.models.pr
import models.ParticipantResult
import models.ResultItem
import models.competitions
import utils.json
import java.io.File
class ResultsViewerActivity : AppCompatActivity() {

    private lateinit var listViewResults: ListView
    private lateinit var btnLoadResults: Button
    private lateinit var btnBackToChoice: Button
    private lateinit var adapter: ResultAdapter
    private val resultList = mutableListOf<ResultItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.arv)

        // Инициализация Views
        listViewResults = findViewById(R.id.list_view_results)
        btnLoadResults = findViewById(R.id.btn_load_results)
        btnBackToChoice = findViewById(R.id.btn_back_to_choice)

        // Инициализация адаптера
        adapter = ResultAdapter(this, resultList)
        listViewResults.adapter = adapter

        // Настройка слушателей
        btnLoadResults.setOnClickListener { loadResultsFromFiles() }
        btnBackToChoice.setOnClickListener { finish() }
    }

    private fun loadResultsFromFiles() {
        // Очищаем старые результаты
        resultList.clear()

        // Получаем папку filesDir
        val filesDir = this.filesDir

        // Ищем файлы с именем result_*.json
        val resultFiles = filesDir.listFiles { file ->
            file.name.startsWith("result_") && file.name.endsWith(".json")
        }

        // Проверяем, нашлись ли файлы
        if (resultFiles != null && resultFiles.isNotEmpty()) {
            // Загружаем конфигурацию соревнования для получения totalMarkers
            val competition: competitions? = json.loadCompetitionFromAssets("competition.json", this)

            // Проходим по каждому файлу результата
            for (file in resultFiles) {
                // Загружаем один результат из файла
                val participantResult: pr? = json.loadParticipantResultFromFile(file.absolutePath)

                // Проверяем, удалось ли загрузить результат
                if (participantResult != null) {
                    // Рассчитываем длительность
                    val durationStr = if (participantResult.finishTime != null && participantResult.startTime != 0L) {
                        val durationMs = participantResult.finishTime!! - participantResult.startTime
                        val durationSecs = durationMs / 1000
                        String.format("%02d:%02d", durationSecs / 60, durationSecs % 60)
                    } else {
                        "N/A"
                    }

                    // Количество найденных закладок
                    val foundMarkers = participantResult.markerDetectionTimes.size

                    // Количество всего закладок (из конфигурации)
                    val totalMarkers = competition?.category?.equals { it.name == participantResult.category }?.totalMarkers ?: 0

                    // Сумма штрафных баллов
                    val penaltyPoints = participantResult.penalties.sumOf { it.appliedPoints }

                    // Создаем объект ResultItem для отображения в списке
                    val item = ResultItem(
                        participantResult.participantId,
                        participantResult.categoryName,
                        durationStr,
                        foundMarkers,
                        totalMarkers,
                        penaltyPoints
                    )
                    // Добавляем в список
                    resultList.add(item)
                } else {
                    // Если не удалось загрузить один файл, выводим сообщение в лог
                    println("Ошибка: не удалось загрузить результат из файла ${file.absolutePath}")
                }
            }

            // Проверяем, были ли успешно загружены какие-то результаты
            if (resultList.isEmpty()) {
                Toast.makeText(this, "Не найдено корректных результатов.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Если файлов не нашлось
            Toast.makeText(this, "Нет сохраненных результатов.", Toast.LENGTH_SHORT).show()
        }

        // Уведомляем адаптер, что данные изменились
        adapter.notifyDataSetChanged()
    }
}
