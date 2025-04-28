package ua.com.merchik.merchik.dataLayer.common

// Датa-классы (уже есть)
class VizitShowcase(
    val id: Int,
    var showcaseId: Int = 0,
    var showcasePhotoId: Int = 0,
    var photoDoId: Int = 0,
    var photoDoHash: String = ""
)


// Хранилище данных
class VizitShowcaseDataHolder private constructor() {
    // Заменяем HashMap на списки дата-классов
    private val vizitShowcases = mutableListOf<VizitShowcase>()

    companion object {
        @Volatile
        private var instance: VizitShowcaseDataHolder? = null

        fun getInstance(): VizitShowcaseDataHolder {
            return instance ?: synchronized(this) {
                instance ?: VizitShowcaseDataHolder().also { instance = it }
            }
        }
    }

    // Добавление данных
    fun addVizitShowcase(showcase: VizitShowcase) {
        vizitShowcases.add(showcase)
    }

    // Получение данных
    fun getVizitShowcases(): List<VizitShowcase> = vizitShowcases.toList()

    // Поиск по ID
    fun findVizitShowcaseById(id: Int): VizitShowcase? {
        return vizitShowcases.find { it.id == id }
    }

    // ✨ Оператор доступа по индексу
    operator fun get(id: Int): VizitShowcase {
        // Ищем ссылку на объект в списке
        val existing = vizitShowcases.find { it.id == id }
        if (existing != null) return existing

        // Если не найдено — создаём и добавляем в список
        val newItem = VizitShowcase(id = id)
        vizitShowcases.add(newItem)
        return newItem
    }

    // Очистка данных
    fun clear() {
        vizitShowcases.clear()
    }
}
