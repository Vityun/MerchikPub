package ua.com.merchik.merchik.dataLayer.common

// Датa-классы (уже есть)
class VizitShowcase(
    val id: Int,
    var showcaseId: Int = 0,
    var showcasePhotoId: Int = 0,
    var photoDoId: Int = 0,
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

//    operator fun get(id: Int): VizitShowcase {
//        return vizitShowcases.find { it.id == id } ?: VizitShowcase(id).also {
//            vizitShowcases.add(it)
//        }
//    }

    // 🔽 Новые методы обновления по ID:

    fun setShowcaseId(id: Int, value: Int) {
        val showcase = vizitShowcases.find { it.id == id }
        if (showcase != null) {
            showcase.showcaseId = value
        } else {
            vizitShowcases.add(VizitShowcase(id = id, showcaseId = value))
        }
    }

    fun setShowcasePhotoId(id: Int, value: Int) {
        val showcase = vizitShowcases.find { it.id == id }
        if (showcase != null) {
            showcase.showcasePhotoId = value
        } else {
            vizitShowcases.add(VizitShowcase(id = id, showcasePhotoId = value))
        }
    }

    fun setPhotoDoId(id: Int, value: Int) {
        val showcase = vizitShowcases.find { it.id == id }
        if (showcase != null) {
            showcase.photoDoId = value
        } else {
            vizitShowcases.add(VizitShowcase(id = id, photoDoId = value))
        }
    }

    // Очистка данных
    fun clear() {
        vizitShowcases.clear()
    }
}

//class VizitShowcaseDataHolder private constructor() {
////    var photoId: Int? = null
////    var photoServerId: String? = null
//    val planogrammVizitMap = HashMap<Int,String>()
//    val planogrammVizitShowcaseMap = HashMap<Int,String>()
//    val planogrammVizitShowcaseMapId = HashMap<Int,Int>()
//
//    val showcase = VizitShowcase(0)
//    val stackPhoto = VizitStackPhoto(0)
//
//    companion object {
//        private var instance: VizitShowcaseDataHolder? = null
//        fun instance(): VizitShowcaseDataHolder {
//            if (instance == null) {
//                instance = VizitShowcaseDataHolder()
//            }
//            return instance!!
//        }
//    }
//
//    fun init() {
////        photoId = null
////        photoServerId = null
//        planogrammVizitMap.clear()
//        planogrammVizitShowcaseMap.clear()
//        planogrammVizitShowcaseMapId.clear()
//    }
//}
//
//data class VizitShowcase(
//    var id: Int,
//    var showcaseId: String? = null,
//    var showcasePhotoId: String? = null
//)
//
//data class VizitStackPhoto(
//    val id: Int,
//    val photoId: String? = null
//)