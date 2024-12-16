package ua.com.merchik.merchik.data.features

interface ExchangeRepository {

//    sendWpData2
//    chatExchange
//    chatGroupExchange
//    tablesLoadingUnloading.uploadAllTables(toolbar_menus.this);     // Выгрузка таблиц
//    tablesLoadingUnloading.downloadAllTables(toolbar_menus.this);   // Скачивание таблиц
//    Загрузка принудительная Образцов фото
//    Загрузка принудительная ФОТОГРАФИЙ Витрин. (Идентификаторов Витрин)
//    загрузка фото за минулі роботи які виконував мерчандайзер, але перевстановив додаток та загубив ці фото

    fun sendWpData2()
    fun uploadComments()
    fun getPhotosToDownload(): List<Int>
//    fun downloadPhotos(photoIds: List<Int>, callback: ResultCallback)
}