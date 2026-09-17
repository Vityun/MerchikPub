package ua.com.merchik.merchik.Activities.TaskAndReclamations.TasksActivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import ua.com.merchik.merchik.Activities.Features.ui.theme.MerchikTheme
import ua.com.merchik.merchik.Globals
import ua.com.merchik.merchik.data.Database.Room.TasksAndReclamationsSDB
import ua.com.merchik.merchik.dataLayer.ContextUI
import ua.com.merchik.merchik.dataLayer.ModeUI
import ua.com.merchik.merchik.dialogs.features.indicator.LineSpinFadeLoaderIndicator
import ua.com.merchik.merchik.features.main.DBViewModels.TovarDBViewModel
import ua.com.merchik.merchik.features.main.Main.MainUI

@AndroidEntryPoint
class TARTovarsFragment : Fragment() {
    private lateinit var tovarViewModel: TovarDBViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(requireActivity()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sourceDad2 = arguments?.getLong(ARG_SOURCE_DAD2) ?: 0L
        val activityContext = requireActivity()
        var loading by mutableStateOf(true)
        var ready by mutableStateOf(false)
        var warning by mutableStateOf<String?>(null)
        var loadJob: Job? = null

        fun loadData() {
            if (loadJob?.isActive == true) return
            loadJob = viewLifecycleOwner.lifecycleScope.launch {
                loading = true
                ready = false
                warning = null
                try {
                    val result = TARTovarDataLoader.load(sourceDad2)
                    val wpData = result.visit
                    // Fragment scope keeps different complaints/source visits independent.
                    tovarViewModel = ViewModelProvider(this@TARTovarsFragment)[TovarDBViewModel::class.java]
                    tovarViewModel.apply {
                        dataJson = Gson().toJson(
                            JSONObject()
                                .put("codeDad2", wpData.code_dad2.toString())
                                .put("clientId", wpData.client_id)
                                .put("addressId", wpData.addr_id)
                        )
                        contextUI = ContextUI.TOVAR_FROM_TOVAR_TABS
                        modeUI = ModeUI.FILTER_SELECT
                        typeWindow = "container"
                        context = activityContext
                        refreshAfterVisitDataLoaded()
                    }
                    warning = result.warnings.takeIf { it.isNotEmpty() }?.joinToString("\n")
                    ready = true
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Globals.writeToMLOG("ERROR", "TARTovars/open", "sourceDad2=$sourceDad2, error=$e")
                    warning = "Не вдалося відкрити товари вихідного відвідування. Спробуйте ще раз."
                } finally {
                    loading = false
                }
            }
        }

        (view as ComposeView).setContent {
            MerchikTheme {
                Column(Modifier.fillMaxSize()) {
                    warning?.let { message ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = ::loadData) {
                                Icon(Icons.Default.Refresh, contentDescription = "Повторити завантаження")
                            }
                        }
                    }
                    if (loading) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            LineSpinFadeLoaderIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                penThickness = 10f,
                                radius = 22f,
                                elementHeight = 15f,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    } else if (ready) {
                        MainUI(
                            modifier = Modifier.weight(1f),
                            viewModel = tovarViewModel,
                            context = activityContext
                        )
                    }
                }
            }
        }
        loadData()
    }

    override fun onDestroyView() {
        if (::tovarViewModel.isInitialized) {
            tovarViewModel.context = null
            tovarViewModel.launcher = null
        }
        super.onDestroyView()
    }

    companion object {
        private const val ARG_SOURCE_DAD2 = "sourceDad2"

        @JvmStatic
        fun newInstance(tar: TasksAndReclamationsSDB) = TARTovarsFragment().apply {
            arguments = Bundle().apply { putLong(ARG_SOURCE_DAD2, tar.codeDad2SrcDoc ?: 0L) }
        }
    }
}
