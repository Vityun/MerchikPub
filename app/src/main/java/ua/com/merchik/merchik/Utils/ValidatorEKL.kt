package ua.com.merchik.merchik.Utils


data class ControlEKLData(
    val result: Boolean,
    val message: String
)

object ValidatorEKL {

    fun controlEKL(t: String): ControlEKLData{
        return ControlEKLData(true,t)
    }

}