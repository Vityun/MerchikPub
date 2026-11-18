package ua.com.merchik.merchik.Activities.ReferencesActivity.Chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ua.com.merchik.merchik.features.main.DBViewModels.ChatSDBViewModel;
import ua.com.merchik.merchik.features.main.chat.ChatConversationContent;
import ua.com.merchik.merchik.features.main.chat.ChatConversationViewModel;

public class ChatFrag extends Fragment {
    private static final String ARG_CHAT_ID = "chat_id";

    public static ChatFrag newInstance(int chatId) {
        ChatFrag fragment = new ChatFrag();
        Bundle args = new Bundle();
        args.putInt(ARG_CHAT_ID, chatId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return new ComposeView(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChatConversationViewModel conversation = new ViewModelProvider(this).get(ChatConversationViewModel.class);
        ChatSDBViewModel chats = new ViewModelProvider(requireActivity()).get(ChatSDBViewModel.class);
        conversation.open(requireArguments().getInt(ARG_CHAT_ID));
        ChatConversationContent.setContentChatConversation((ComposeView) view, conversation, chats, this::closeChat);
    }

    private void closeChat() {
        if (getParentFragmentManager().isStateSaved()) return;
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            getParentFragmentManager().beginTransaction().remove(this).commit();
        }
    }
}
