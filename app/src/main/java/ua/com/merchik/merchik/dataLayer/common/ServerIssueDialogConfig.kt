package ua.com.merchik.merchik.dataLayer.common

data class ServerIssueDialogConfig(
    val message: String,
    val positiveText: String,
    val cancelText: String? = null,
    val onPositiveClick: () -> Unit,
    val onTextLinkClick: (String) -> Unit
)