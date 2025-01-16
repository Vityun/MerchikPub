package ua.com.merchik.merchik.dialogs.EKL

class EKLDataHolder private constructor() {

    var usersPTTid: Int? = null
    var usersPTTName: String? = null
    var usersPTTWorkAddressId: Int? = null
    var usersPTTWPClientId: String? = null
    var usersPTTWPPttUserId: Int? = null
    var usersPTTWPDataUserId: Int? = null
    var usersPTTWPDataTime: Long? = null
    var usersPTTNumberTel1: String? = null
    var usersPTTNumberTel2: String? = null
    var usersPTTtovarIdList: List<Int> = emptyList()


    companion object {
        private var instance: EKLDataHolder? = null
        fun instance(): EKLDataHolder {
            if (instance == null) {
                instance = EKLDataHolder()
            }
            return instance!!
        }
    }

    fun init() {
        usersPTTid = null
        usersPTTName = null
        usersPTTWorkAddressId = null
        usersPTTWPClientId = null
        usersPTTWPPttUserId = null
        usersPTTWPDataUserId = null
        usersPTTWPDataTime = null
        usersPTTNumberTel1 = null
        usersPTTNumberTel2 = null
        usersPTTtovarIdList = emptyList()
    }
}