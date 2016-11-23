package com.madpixels.tgadmintools.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.madpixels.apphelpers.MyToast;
import com.madpixels.apphelpers.Sets;
import com.madpixels.apphelpers.UIUtils;
import com.madpixels.apphelpers.Utils;
import com.madpixels.apphelpers.ui.ActivityExtended;
import com.madpixels.apphelpers.ui.ProgressDialogBuilder;
import com.madpixels.tgadmintools.Const;
import com.madpixels.tgadmintools.R;
import com.madpixels.tgadmintools.db.DBHelper;
import com.madpixels.tgadmintools.entities.ChatTask;
import com.madpixels.tgadmintools.entities.ChatTaskControl;
import com.madpixels.tgadmintools.helper.DialogInputValue;
import com.madpixels.tgadmintools.helper.TgH;
import com.madpixels.tgadmintools.helper.TgUtils;
import com.madpixels.tgadmintools.services.ServiceChatTask;
import com.madpixels.tgadmintools.utils.TgImageGetter;

import org.drinkless.td.libcore.telegram.Client;
import org.drinkless.td.libcore.telegram.TdApi;

import java.util.concurrent.TimeUnit;

import io.github.rockerhieu.emojicon.EmojiconEditText;
import io.github.rockerhieu.emojicon.EmojiconTextView;
import libs.AdHelper;

/**
 * Created by Snake on 29.02.2016.
 */
public class ActivityGroupInfo extends ActivityExtended {

    ImageButton btnAva;
    TextView tvChatUsername, tvUsersCount, tvAdminsCount, tvKickedCount, tvBanLinksWarningCount, tvBanSticksWarningCount,
            tvInviteLink, tvChatType, tvBanWordsAllowCount, tvBtnWarnLinksFreq, tvBtnWarnStickersFreq,
            tvBtnWarnBanWordsFreq, tvBanVoiceWarningCount, tvBtnWarnVoiceFreq, tvFloodMsgAllowCount,
            tvBtnWarnBanFloodFreq, tvChatCommandsCount;
    EmojiconTextView tvTitle, tvChatDescription, tvChatAdmin;
    View viewContent, viewLoading, layerBanLinksAge, layerBanSticksTime, layerBanForBlackWord,
            layerBanVoiceTime, layerFloodControl;
    Button btnConvertToSuper, btnEditCommands;
    CheckBox chkAnyoneInviteFriendsSuper, chkAnyoneManageGroup, chkRemoveLinks, chkBanForLinks, chkRemoveStickers,
            chkBanForSticks, chkReturnOnBanLinksExpired, chkReturnOnBanSticksExpired,
            chkWelcomeText, chkReturnOnBannedWordsExpired, chkBlackListedWordsEnabled,
            chkRemoveJoinedMsg, chkRemoveLeaveMsg, chkRemoveVoices, chkBanForVoice, chkReturnOnBanVoiceExpired,
            chkFloodControlEnabled, chkBanFloodUser, chkReturnOnBanFloodExpired, chkCommandsEnable;
    EditText edtLinksBanTimeVal, edtSticksBanTimeVal, edtWelcomeText,
            edtBannedWordBanTimesVal, edtLinksFloodTimeVal, edtStickersFloodTimeVal,
            edtBanWordsFloodTimeVal, edtVoiceBanTimeVal, edtVoiceFloodTimeVal, edtFloodControlTimeVal,
            edtFloodBanTimeVal;
    Spinner spinnerLinksBanFloodTime, spinnerStickersFloodTime, spinnerBanTimesSticks, spinnerBanTimesLink,
            spinnerBanWordsFloodTime, spinnerBannedWordBanTimes, spinnerBanVoiceTime, spinnerVoiceFloodTime,
            spinnerFloodControlTime, spinnerBanFloodTime;
    SeekBar seekBarBanVoiceAllowCount;
    private PopupWindow mCurrentPopupWindow;


    int chatType;
    long chatId;
    int groupId, channelId;
    String chatTitle, chatUsername, chatDescription, chatInviteLink;
    int chatUsersCount, adminsCount, kickedCount;
    boolean isAdmin = false; // creator
    boolean superAanyoneCanInvite, groupAnyoneCanEdit;
    SeekBar seekBarBanLinksCount, seekBarBanSticksCount;

    String adminName;
    int adminId = 0;
    boolean isChannel = false;

    private TdApi.GroupFull groupFull;

    ChatTaskControl chatTasks;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("chatType", chatType);
        outState.putInt("channel_id", channelId);
        outState.putInt("group_id", groupId);
        outState.putLong("chat_id", chatId);
        outState.putString("title", chatTitle);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        UIUtils.setToolbarWithBackArrow(this, R.id.toolbar);
        AdHelper.showBanner(findViewById(R.id.adView));

        Bundle b = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        chatType = b.getInt("chatType");
        channelId = b.getInt("channel_id");
        groupId = b.getInt("group_id");
        chatTitle = b.getString("title");
        chatId = b.getLong("chat_id");


        viewContent = getView(R.id.scrollViewMainContent);
        viewLoading = getView(R.id.prgLoading);
        viewContent.setVisibility(View.GONE);


        UIUtils.include(this, R.id.layer_words_antispam, R.layout.layout_words_antispam);
        UIUtils.include(this, R.id.layer_stickers_antispam, R.layout.layout_stickers_antispam);
        UIUtils.include(this, R.id.layer_links_antispam, R.layout.layout_links_antispam);
        UIUtils.include(this, R.id.layer_voice_antispam, R.layout.layout_voices_antispam);
        UIUtils.include(this, R.id.layer_flood_control, R.layout.layout_flood_control);
        UIUtils.include(this, R.id.layer_commands, R.layout.layout_commands);

        btnAva = getView(R.id.imgBtnChatAva);
        btnConvertToSuper = getView(R.id.btnConvertToSuper);
        tvChatUsername = getView(R.id.tvChatUsername);
        tvTitle = getView(R.id.tvTitle);
        tvChatDescription = getView(R.id.tvChatDescription);
        tvUsersCount = getView(R.id.tvUsersCount);
        tvChatAdmin = getView(R.id.tvMainAdmin);
        tvAdminsCount = getView(R.id.tvAdminsCount);
        tvKickedCount = getView(R.id.tvKickedCount);
        seekBarBanLinksCount = getView(R.id.seekBarBanLinksCount);
        seekBarBanSticksCount = getView(R.id.seekBarBanSticksCount);
        tvBanSticksWarningCount = getView(R.id.tvBanSticksWarningCount);
        tvBanLinksWarningCount = getView(R.id.tvBanLinksAllowCount);
        tvInviteLink = getView(R.id.tvInviteLink);
        btnEditCommands = getView(R.id.btnEditCommands);
        chkCommandsEnable = getView(R.id.chkCommandsEnable);
        tvChatCommandsCount = getView(R.id.tvChatCommandsCount);

        layerBanLinksAge = getView(R.id.layerBanLinksAge);
        layerBanSticksTime = getView(R.id.layerBanSticksTime);
        chkReturnOnBanSticksExpired = getView(R.id.chkReturnOnBanSticksExpired);
        chkReturnOnBanLinksExpired = getView(R.id.chkReturnOnBanLinksExpired);
        tvChatType = getView(R.id.tvChatType);
        edtWelcomeText = getView(R.id.edtWelcomeText);
        chkWelcomeText = getView(R.id.chkWelcomeText);
        chkReturnOnBannedWordsExpired = getView(R.id.chkReturnOnBannedWordsExpired);
        layerBanForBlackWord = getView(R.id.layerBanForBlackWord);
        chkRemoveJoinedMsg = getView(R.id.chkRemoveJoinedMsg);
        chkRemoveLeaveMsg = getView(R.id.chkRemoveLeaveMsg);
        //edtBanWordWarnText = getView(R.id.edtBanWordWarnText);
        tvBanWordsAllowCount = getView(R.id.tvBanWordsAllowCount);
        TextView tvNoticePhoneBookEnabled = getView(R.id.tvNoticePhoneBookEnabled);
        tvBtnWarnLinksFreq = getView(R.id.tvBtnWarnLinksFreq);
        edtStickersFloodTimeVal = getView(R.id.edtStickersFloodTimeVal);
        spinnerStickersFloodTime = getView(R.id.spinnerStickersFloodTime);
        tvBtnWarnStickersFreq = getView(R.id.tvBtnWarnStickersFreq);
        tvBtnWarnBanWordsFreq = getView(R.id.tvBtnWarnBanWordsFreq);
        chkBlackListedWordsEnabled = getView(R.id.chkBlackListedWordsEnabled);
        edtBanWordsFloodTimeVal = getView(R.id.edtBanWordsFloodTimeVal);
        spinnerBanWordsFloodTime = getView(R.id.spinnerBanWordsFloodTime);
        spinnerLinksBanFloodTime = getView(R.id.spinnerLinksBanFloodTime);
        edtLinksFloodTimeVal = getView(R.id.edtLinksBanFloodTimeVal);
        spinnerBannedWordBanTimes = getView(R.id.spinnerBannedWordBanTimes);
        edtBannedWordBanTimesVal = getView(R.id.edtBannedWordBanTimesVal);
        spinnerBanTimesSticks = getView(R.id.spinnerBanSticksAge);
        edtSticksBanTimeVal = getView(R.id.edtSticksBanTimeVal);
        layerBanVoiceTime = getView(R.id.layerBanVoiceTime);
        edtVoiceFloodTimeVal = getView(R.id.edtVoiceFloodTimeVal);
        chkFloodControlEnabled = getView(R.id.chkFloodControlEnabled);
        tvFloodMsgAllowCount = getView(R.id.tvFloodMsgAllowCount);

        chkRemoveVoices = getView(R.id.chkRemoveVoices);
        chkBanForVoice = getView(R.id.chkBanForVoice);
        chkReturnOnBanVoiceExpired = getView(R.id.chkReturnOnBanVoiceExpired);

        edtVoiceBanTimeVal = getView(R.id.edtVoiceBanTimeVal);
        seekBarBanVoiceAllowCount = getView(R.id.seekBarBanVoiceAllowCount);
        tvBanVoiceWarningCount = getView(R.id.tvBanVoiceWarningCount);
        tvBtnWarnVoiceFreq = getView(R.id.tvBtnWarnVoiceFreq);
        spinnerBanVoiceTime = getView(R.id.spinnerBanVoiceTime);
        spinnerVoiceFloodTime = getView(R.id.spinnerVoiceFloodTime);
        edtFloodControlTimeVal = getView(R.id.edtFloodControlTimeVal);
        spinnerFloodControlTime = getView(R.id.spinnerFloodControlTime);
        chkBanFloodUser = getView(R.id.chkBanFloodUser);
        chkReturnOnBanFloodExpired = getView(R.id.chkReturnOnBanFloodExpired);
        edtFloodBanTimeVal = getView(R.id.edtFloodBanTimeVal);
        spinnerBanFloodTime = getView(R.id.spinnerBanFloodTime);
        layerFloodControl = getView(R.id.layerFloodControl);
        tvBtnWarnBanFloodFreq = getView(R.id.tvBtnWarnBanFloodFreq);

        TextView tvBannedWordsCount = getView(R.id.tvBannedWordsCount);

        setTitle(R.string.title_group_info);
        tvTitle.setText(chatTitle);

        UIUtils.setBatchClickListener(onClickListener, tvChatUsername, btnConvertToSuper, tvTitle, tvChatAdmin, tvChatDescription,
                tvInviteLink, tvAdminsCount, tvUsersCount, tvKickedCount, chkReturnOnBannedWordsExpired,
                tvBanLinksWarningCount, tvBanSticksWarningCount, tvBanWordsAllowCount, tvBannedWordsCount,
                tvBtnWarnLinksFreq, tvBtnWarnStickersFreq, tvBtnWarnBanWordsFreq, chkBlackListedWordsEnabled,
                chkRemoveVoices, chkBanForVoice, tvBtnWarnVoiceFreq, chkReturnOnBanVoiceExpired, tvBanVoiceWarningCount,
                chkFloodControlEnabled, tvFloodMsgAllowCount, chkBanFloodUser, chkReturnOnBanFloodExpired,
                tvBtnWarnBanFloodFreq, tvNoticePhoneBookEnabled, btnEditCommands, chkCommandsEnable);

        if (Sets.getBoolean(Const.ANTISPAM_IGNORE_SHARED_CONTACTS, Const.ANTISPAM_IGNORE_SHARED_CONTACTS_DEFAULT)) {
            tvNoticePhoneBookEnabled.setText(getString(R.string.label_notice_ignore_antispam_for_shared));
        } else
            tvNoticePhoneBookEnabled.setVisibility(View.GONE);

        chkAnyoneInviteFriendsSuper = getView(R.id.chkAnyoneInviteFriendsSuper);
        chkAnyoneManageGroup = getView(R.id.chkAnyoneManageGroup);
        if (TgUtils.isGroup(chatType)) {
            chkAnyoneInviteFriendsSuper.setVisibility(View.GONE);
            tvChatUsername.setVisibility(View.GONE);
            findViewById(R.id.viewChatDescription).setVisibility(View.GONE);
        } else {
            chkAnyoneManageGroup.setVisibility(View.GONE);
        }
        UIUtils.setBatchClickListener(onClickListener, chkAnyoneInviteFriendsSuper, chkAnyoneManageGroup);

        chkRemoveLinks = getView(R.id.chkRemoveLinks);
        chkBanForLinks = getView(R.id.chkBanForLinks);
        chkRemoveStickers = getView(R.id.chkRemoveStickers);
        chkBanForSticks = getView(R.id.chkBanForSticks);

        UIUtils.setBatchClickListener(onClickListener, chkRemoveLinks, chkRemoveStickers, chkBanForSticks, chkBanForLinks,
                chkReturnOnBanLinksExpired, chkReturnOnBanSticksExpired,
                chkRemoveLeaveMsg, chkRemoveJoinedMsg);

        spinnerBanTimesLink = getView(R.id.spinnerBanLinksAge);
        edtLinksBanTimeVal = getView(R.id.edtLinksBanTimeVal);

        chatTasks = new ChatTaskControl(chatId);

        setTask(ChatTask.TYPE.VOICE);
        setTask(ChatTask.TYPE.STICKERS);
        setTask(ChatTask.TYPE.LINKS);


        setSpinnerBanTimesListener(ChatTask.TYPE.BANWORDS, spinnerBannedWordBanTimes, edtBannedWordBanTimesVal, chkReturnOnBannedWordsExpired);
        setSpinnerFloodSelectorListener(ChatTask.TYPE.BANWORDS, spinnerBanWordsFloodTime, edtBanWordsFloodTimeVal);

        setSpinnerBanTimesListener(ChatTask.TYPE.FLOOD, spinnerBanFloodTime, edtFloodBanTimeVal, chkReturnOnBanFloodExpired);
        setSpinnerFloodSelectorListener(ChatTask.TYPE.FLOOD, spinnerFloodControlTime, edtFloodControlTimeVal);


        if (TgUtils.isSuperGroup(chatType)) {
            btnConvertToSuper.setVisibility(View.GONE);
        } else {
            tvChatType.setText(R.string.chatTypeGroup);
            UIUtils.setTextColotRes(chkRemoveLinks, R.color.md_grey_500);
            UIUtils.setTextColotRes(chkRemoveStickers, R.color.md_grey_500);
            UIUtils.setTextColotRes(chkRemoveJoinedMsg, R.color.md_grey_500);
            UIUtils.setTextColotRes(chkRemoveLeaveMsg, R.color.md_grey_500);
        }


        if (!chatTasks.isEmpty()) {
            ChatTask taskJoinMsg = chatTasks.getTask(ChatTask.TYPE.JoinMsg, false);
            if (taskJoinMsg != null) {
                chkRemoveJoinedMsg.setChecked(taskJoinMsg.isEnabled);
            }

            ChatTask taskFlood = chatTasks.getTask(ChatTask.TYPE.FLOOD, false);
            if (taskFlood != null) {
                if (taskFlood.isEnabled)
                    chkFloodControlEnabled.setChecked(true);
                else
                    layerFloodControl.setVisibility(View.GONE);

                chkBanFloodUser.setChecked(taskFlood.isBanUser);
                tvFloodMsgAllowCount.setText(taskFlood.mAllowCountPerUser + "");
                chkReturnOnBanFloodExpired.setChecked(taskFlood.isReturnOnBanExpired);
                if (!taskFlood.isBanUser) {
                    edtFloodBanTimeVal.setEnabled(false);
                    spinnerBanFloodTime.setEnabled(false);
                    chkReturnOnBanFloodExpired.setEnabled(false);

                }

                if (taskFlood.mWarningsDuringTime == 0) {
                    edtFloodControlTimeVal.setTag("1");
                    edtFloodControlTimeVal.setText("5");
                    spinnerFloodControlTime.setTag(1);// flag for skip save changes
                    spinnerFloodControlTime.setSelection(0);
                } else {
                    setSecondsToFormat(taskFlood.mWarningsDuringTime, edtFloodControlTimeVal, spinnerFloodControlTime);
                }

                if (taskFlood.mBanTimeSec > 0) {
                    setSecondsToFormat(taskFlood.mBanTimeSec, edtFloodBanTimeVal, spinnerBanFloodTime);
                } else {// 0 - permanent ban.

                    edtFloodBanTimeVal.setTag("1");
                    edtFloodBanTimeVal.setText("5");
                    chkReturnOnBanFloodExpired.setEnabled(false);
                    edtFloodBanTimeVal.setEnabled(false);
                    spinnerBanFloodTime.setTag(1);
                    spinnerBanFloodTime.setSelection(0);
                }

                setWarnFrequencyText(tvBtnWarnBanFloodFreq, taskFlood.mWarnFrequency);


            } else {
                setDefaultFloodValues();
            }

            ChatTask taskLeaveMsg = chatTasks.getTask(ChatTask.TYPE.LeaveMsg, false);
            if (taskLeaveMsg != null) {
                chkRemoveLeaveMsg.setChecked(taskLeaveMsg.isEnabled);
            }

            ChatTask taskBanWords = chatTasks.getTask(ChatTask.TYPE.BANWORDS, false);
            if (taskBanWords != null) {
                chkBlackListedWordsEnabled.setChecked(taskBanWords.isEnabled);

                if (taskBanWords.mBanTimeSec > 0) {
                    setSecondsToFormat(taskBanWords.mBanTimeSec, edtBannedWordBanTimesVal, spinnerBannedWordBanTimes);
                } else {// 0 - permanent ban.
                    edtBannedWordBanTimesVal.setTag("1");
                    edtBannedWordBanTimesVal.setText("5");
                    chkReturnOnBannedWordsExpired.setEnabled(false);
                    edtBannedWordBanTimesVal.setEnabled(false);
                    spinnerBannedWordBanTimes.setTag(1);
                    spinnerBannedWordBanTimes.setSelection(0);
                }

                tvBanWordsAllowCount.setText(taskBanWords.mAllowCountPerUser + "");
                chkReturnOnBannedWordsExpired.setChecked(taskBanWords.isReturnOnBanExpired);

                if (taskBanWords.mWarningsDuringTime == 0) {
                    edtBanWordsFloodTimeVal.setTag("1");
                    edtBanWordsFloodTimeVal.setText("5");
                    spinnerBanWordsFloodTime.setTag(1);// flag for skip save changes
                    spinnerBanWordsFloodTime.setSelection(0);
                } else {
                    setSecondsToFormat(taskBanWords.mWarningsDuringTime, edtBanWordsFloodTimeVal, spinnerBanWordsFloodTime);
                }

                if (!taskBanWords.isEnabled)
                    layerBanForBlackWord.setVisibility(View.GONE);
                setWarnFrequencyText(tvBtnWarnBanWordsFreq, taskBanWords.mWarnFrequency);
            } else {
                setDefaultBannedWordsValues();
            }

            ChatTask taskCommands = chatTasks.getTask(ChatTask.TYPE.COMMAND, false);
            if (taskCommands != null) {
                chkCommandsEnable.setChecked(taskCommands.isEnabled);
                if (!taskCommands.isEnabled) {
                    btnEditCommands.setEnabled(false);
                }

                int chatCommandsCount = DBHelper.getInstance().getChatCommandsCount(chatId);
                tvChatCommandsCount.setText(chatCommandsCount+" commands for this chat");

            } else {
                btnEditCommands.setEnabled(false);
            }

        } else {
            setDefaultBannedWordsValues();
            setDefaultFloodValues();
            btnEditCommands.setEnabled(false);
        }

        int blackWordsCount = DBHelper.getInstance().getWordsBlackListCount(chatId);
        if (blackWordsCount == 0) {
            tvBannedWordsCount.setText(R.string.btn_banned_words_list_empty);
        } else {
            tvBannedWordsCount.setText(getString(R.string.banned_words_count, blackWordsCount));
            //tvBannedWordsCount.setText(String.format("%d banned words for this chat", blackWordsCount));
        }
        /**
         * DONE срок бана слов по умолчанию 5 минут сделать
         * сколько раз можно: по умолчанию показывать 3.         *
         */

        setEditTextChangeListenerToSaveBanTimeValue(ChatTask.TYPE.BANWORDS);
        setEditTextChangeListenerToSaveBanTimeValue(ChatTask.TYPE.FLOOD);

        setWelcomeText();
        getChatInfo();
    }

    private void setDefaultBannedWordsValues() {
        spinnerBannedWordBanTimes.setTag(1);
        spinnerBannedWordBanTimes.setSelection(1);
        tvBanWordsAllowCount.setText("3");
        layerBanForBlackWord.setVisibility(View.GONE);
        edtBanWordsFloodTimeVal.setTag("1");
        edtBanWordsFloodTimeVal.setText("1");
        spinnerBanWordsFloodTime.setTag(1);
        spinnerBanWordsFloodTime.setSelection(2);
        chkReturnOnBannedWordsExpired.setChecked(true);
    }

    private void setDefaultFloodValues() {
        layerFloodControl.setVisibility(View.GONE);
        tvFloodMsgAllowCount.setText("10");
        edtFloodControlTimeVal.setTag("1");
        edtFloodControlTimeVal.setText("1");
        spinnerFloodControlTime.setTag(1);
        spinnerFloodControlTime.setSelection(1);
        spinnerBanFloodTime.setTag(1);
        spinnerBanFloodTime.setSelection(1);
        tvBtnWarnBanFloodFreq.setText(R.string.text_warn_on_last_warn);

        edtFloodBanTimeVal.setEnabled(false);
        spinnerBanFloodTime.setEnabled(false);
        chkReturnOnBanFloodExpired.setEnabled(false);
    }

    void setTask(ChatTask.TYPE pType) {
        Spinner spinnerBanTime = null, spinnerFloodTimeLimit = null;
        EditText edtBanTimeValue = null, edtFloodTimeValue = null;
        CheckBox chkReturnOnBanExpired = null, chkRemoveMessage = null, chkBanUser = null;
        TextView tvWarningsCount = null, tvBtnWarnFreq = null;
        SeekBar seekBarAllowCount = null;
        View layerBanTimes = null;
        ChatTask task = null;
        task = chatTasks.getTask(pType, false);
        switch (pType) {
            case VOICE:
                spinnerBanTime = spinnerBanVoiceTime;
                edtBanTimeValue = edtVoiceBanTimeVal;
                spinnerFloodTimeLimit = spinnerVoiceFloodTime;
                edtFloodTimeValue = edtVoiceFloodTimeVal;
                chkReturnOnBanExpired = chkReturnOnBanVoiceExpired;
                chkRemoveMessage = chkRemoveVoices;
                chkBanUser = chkBanForVoice;
                seekBarAllowCount = seekBarBanVoiceAllowCount;

                tvWarningsCount = tvBanVoiceWarningCount;
                tvBtnWarnFreq = tvBtnWarnVoiceFreq;
                layerBanTimes = layerBanVoiceTime;
                break;
            case STICKERS:
                spinnerBanTime = spinnerBanTimesSticks;
                edtBanTimeValue = edtSticksBanTimeVal;
                spinnerFloodTimeLimit = spinnerStickersFloodTime;
                edtFloodTimeValue = edtStickersFloodTimeVal;
                chkReturnOnBanExpired = chkReturnOnBanSticksExpired;
                chkRemoveMessage = chkRemoveStickers;
                chkBanUser = chkBanForSticks;
                seekBarAllowCount = seekBarBanSticksCount;

                tvWarningsCount = tvBanSticksWarningCount;
                tvBtnWarnFreq = tvBtnWarnStickersFreq;
                layerBanTimes = layerBanSticksTime;
                break;
            case LINKS:
                spinnerBanTime = spinnerBanTimesLink;
                edtBanTimeValue = edtLinksBanTimeVal;
                spinnerFloodTimeLimit = spinnerLinksBanFloodTime;
                edtFloodTimeValue = edtLinksFloodTimeVal;
                chkReturnOnBanExpired = chkReturnOnBanLinksExpired;
                chkRemoveMessage = chkRemoveLinks;
                chkBanUser = chkBanForLinks;
                seekBarAllowCount = seekBarBanLinksCount;

                tvWarningsCount = tvBanLinksWarningCount;
                tvBtnWarnFreq = tvBtnWarnLinksFreq;
                layerBanTimes = layerBanLinksAge;
                break;
        }

        setSpinnerFloodSelectorListener(pType, spinnerFloodTimeLimit, edtFloodTimeValue);
        setSpinnerBanTimesListener(pType, spinnerBanTime, edtBanTimeValue, chkReturnOnBanExpired);
        setEditTextChangeListenerToSaveBanTimeValue(pType);

        seekBarAllowCount.setTag(pType);
        seekBarAllowCount.setOnSeekBarChangeListener(onSeekBarBanCountListener);

        if (task != null) {
            chkRemoveMessage.setChecked(task.isRemoveMessage);
            chkBanUser.setChecked(task.isBanUser);
            if (task.isBanUser)
                chkBanUser.setVisibility(View.VISIBLE);

            seekBarAllowCount.setProgress(task.mAllowCountPerUser);

            if (!task.isBanUser)
                layerBanTimes.setVisibility(View.GONE);
            if (task.mBanTimeSec > 0) {
                setSecondsToFormat(task.mBanTimeSec, edtBanTimeValue, spinnerBanTime);
            } else {// 0 - permanent ban.
                edtBanTimeValue.setEnabled(false);
                spinnerBanTime.setTag(1);
                spinnerBanTime.setSelection(0);
            }

            chkReturnOnBanExpired.setChecked(task.isReturnOnBanExpired);
            chkReturnOnBanExpired.setEnabled(task.mBanTimeSec > 0);
            tvWarningsCount.setText(task.mAllowCountPerUser + "");

            if (task.mWarningsDuringTime == 0) {
                edtFloodTimeValue.setTag("1");// flag for ignore save on change
                edtFloodTimeValue.setText("5");
                spinnerFloodTimeLimit.setTag(1);// flag for skip save changes
                spinnerFloodTimeLimit.setSelection(0);
                edtFloodTimeValue.setEnabled(false);
            } else {
                setSecondsToFormat(task.mWarningsDuringTime, edtFloodTimeValue, spinnerFloodTimeLimit);
            }
            setWarnFrequencyText(tvBtnWarnFreq, task.mWarnFrequency);
        } else {
            edtBanTimeValue.setEnabled(true);
            layerBanTimes.setVisibility(View.GONE);
            spinnerBanTime.setTag(1);
            spinnerBanTime.setSelection(1);
            spinnerFloodTimeLimit.setTag(1);
            spinnerFloodTimeLimit.setSelection(1);
            chkReturnOnBanExpired.setChecked(true);
            tvWarningsCount.setText("3");
        }
    }

    void setEditTextChangeListenerToSaveBanTimeValue(final ChatTask.TYPE pType) {
        final Spinner spinnerTimeMultiplier;
        final EditText edit;
        final CheckBox chkReturnOnBanExired;

        switch (pType) {
            case STICKERS:
                edit = edtSticksBanTimeVal;
                chkReturnOnBanExired = chkReturnOnBanSticksExpired;
                spinnerTimeMultiplier = spinnerBanTimesSticks;
                break;
            case LINKS:
                edit = edtLinksBanTimeVal;
                chkReturnOnBanExired = chkReturnOnBanLinksExpired;
                spinnerTimeMultiplier = spinnerBanTimesLink;
                break;
            case BANWORDS:
                edit = edtBannedWordBanTimesVal;
                chkReturnOnBanExired = chkReturnOnBannedWordsExpired;
                spinnerTimeMultiplier = spinnerBannedWordBanTimes;
                break;
            case VOICE:
                edit = edtVoiceBanTimeVal;
                chkReturnOnBanExired = chkReturnOnBanVoiceExpired;
                spinnerTimeMultiplier = spinnerBanVoiceTime;
                break;
            case FLOOD:
                edit = edtFloodBanTimeVal;
                chkReturnOnBanExired = chkReturnOnBanFloodExpired;
                spinnerTimeMultiplier = spinnerBanFloodTime;
                break;
            default:
                edit = null;
                spinnerTimeMultiplier = null;
                chkReturnOnBanExired = null;
                break;
        }

        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edit.getTag() != null && edit.getTag().toString().equals("1"))// ignore when set default value
                    edit.setTag(null);
                else
                    edit.setTag(System.currentTimeMillis());
            }

            @Override
            public void afterTextChanged(Editable s) {
                edit.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (edit.getTag() == null)
                            return;

                        long ts = Long.valueOf(edit.getTag().toString());
                        if (System.currentTimeMillis() - ts < 900)
                            return;

                        long val = setBanAgeForTask(pType, spinnerTimeMultiplier.getSelectedItemPosition(), edit.getText().toString());
                        if (val > -1)
                            chkReturnOnBanExired.setEnabled(val > 0);
                    }
                }, 1000);
            }
        });

    }

    private void setWarnFrequencyText(TextView textView, int warnFrequency) {
        int resIDWarnText = 0;
        switch (warnFrequency) {
            case 0:
                resIDWarnText = R.string.text_warn_no_warn_user;
                break;
            case 1:
                resIDWarnText = R.string.text_warn_on_last_warn;
                break;
            case 2:
                resIDWarnText = R.string.text_warn_on_first_warn;
                break;
            case 3:
                resIDWarnText = R.string.text_warn_on_first_last_warn;
                break;
            case 4:
                resIDWarnText = R.string.text_warn_on_second_and_last_warn;
                break;
            case 5:
                resIDWarnText = R.string.text_warn_always;
                break;
        }
        textView.setText(resIDWarnText);
    }

    private void setSpinnerBanTimesListener(final ChatTask.TYPE pType, final Spinner spinerBanTime, final EditText edtBanTimeVal, final CheckBox chkReturnOnExpired) {
        String[] banTimes = getResources().getStringArray(R.array.auto_ban_times);
        ArrayAdapter<String> pLinksAgessAdapter = new ArrayAdapter<String>(
                mContext, R.layout.item_spinner, R.id.textView, banTimes);

        spinerBanTime.setAdapter(pLinksAgessAdapter);
        spinerBanTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinerBanTime.getTag() != null)//if has tag we skip saving changes.
                    spinerBanTime.setTag(null);//just clear prevous state
                else {
                    edtBanTimeVal.setEnabled(position > 0);
                    chkReturnOnExpired.setEnabled(position > 0);

                    setBanAgeForTask(pType, position, edtBanTimeVal.getText().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setSpinnerFloodSelectorListener(final ChatTask.TYPE pType, final Spinner spinner, final EditText edtValue) {
        String[] banWithinTimes = getResources().getStringArray(R.array.times_during_warnings);
        ArrayAdapter<String> pTimesAdapter = new ArrayAdapter<String>(
                mContext, R.layout.item_spinner, R.id.textView, banWithinTimes);
        spinner.setAdapter(pTimesAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinner.getTag() != null)
                    spinner.setTag(null);
                else {
                    edtValue.setEnabled(position > 0);
                    saveTaskWarnWithinTime(pType, position, edtValue.getText().toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        edtValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (edtValue.getTag() != null && edtValue.getTag().toString().equals("1"))
                    edtValue.setTag(null);
                else
                    edtValue.setTag(System.currentTimeMillis());
            }

            @Override
            public void afterTextChanged(Editable s) {
                edtValue.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (edtValue.getTag() == null)
                            return;

                        long ts = Long.valueOf(edtValue.getTag().toString());
                        if (System.currentTimeMillis() - ts < 900)
                            return;

                        saveTaskWarnWithinTime(pType, spinner.getSelectedItemPosition(), edtValue.getText().toString());
                    }
                }, 1000);

            }
        });
    }


    private void saveTaskWarnWithinTime(ChatTask.TYPE type, int position, String valueStr) {
        ChatTask task = chatTasks.getTask(type);
        int value;
        try {
            value = Integer.valueOf(valueStr);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return;
        }
        switch (position) {
            case 0:
                task.mWarningsDuringTime = 0;
                break;
            case 1:
                task.mWarningsDuringTime = TimeUnit.MINUTES.toSeconds(value);
                break;
            case 2:
                task.mWarningsDuringTime = TimeUnit.HOURS.toSeconds(value);
                break;
            case 3:
                task.mWarningsDuringTime = TimeUnit.DAYS.toSeconds(value);
                break;
        }
        chatTasks.saveTask(task);
    }


    /**
     * Spinner must have values: 0-Forever, 1-minutes, 2-hours, 3 - days.
     */
    void setSecondsToFormat(long seconds, EditText editText, Spinner spinnerForSelection) {
        String text;
        int spinnerPos;
        if (seconds >= TimeUnit.DAYS.toSeconds(1)) {
            spinnerPos = 3;//days selection
            text = String.valueOf((TimeUnit.SECONDS.toDays(seconds)));
        } else if (seconds >= TimeUnit.HOURS.toSeconds(1)) {
            spinnerPos = 2;// selection hours
            text = String.valueOf(TimeUnit.SECONDS.toHours(seconds));
        } else {//minutes
            spinnerPos = 1;// minutes
            text = String.valueOf(TimeUnit.SECONDS.toMinutes(seconds));
        }

        editText.setTag("1");// flag for ignore save on change
        editText.setText(text);
        spinnerForSelection.setTag(1);// flag for skip save changes
        spinnerForSelection.setSelection(spinnerPos);
    }

    private void setWelcomeText() {
        edtWelcomeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                edtWelcomeText.setTag(System.currentTimeMillis());
            }

            @Override
            public void afterTextChanged(Editable s) {
                edtWelcomeText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long ts = Long.valueOf(edtWelcomeText.getTag().toString());
                        if (System.currentTimeMillis() - ts < 900) return;
                        String text = edtWelcomeText.getText().toString().trim();
                        DBHelper.getInstance().setWelcomeText(chatId, text, chkWelcomeText.isChecked());
                        if (chkWelcomeText.isChecked())
                            ServiceChatTask.start(mContext);
                    }
                }, 1000);
            }
        });

        chkWelcomeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DBHelper.getInstance().setWelcomeText(chatId, edtWelcomeText.getText().toString().trim(), chkWelcomeText.isChecked());
                if (chkWelcomeText.isChecked())
                    ServiceChatTask.start(mContext);
            }
        });

        Object[] welcomeTexts = DBHelper.getInstance().getWelcomeTextObject(chatId);
        if (welcomeTexts == null) return;
        boolean isEnabled = (boolean) welcomeTexts[0];
        String text = welcomeTexts[1].toString();
        chkWelcomeText.setChecked(isEnabled);
        edtWelcomeText.setText(text);


    }

    long setBanAgeForTask(ChatTask.TYPE pType, int selection, String textValue) {
        long banAge = 0;
        int val;

        try {
            val = Integer.valueOf(textValue);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return -1;
        }

        switch (selection) {
            case 0:
                banAge = 0; // forever ban
                break;
            case 1:
                banAge = TimeUnit.MINUTES.toSeconds(val);
                break;
            case 2:
                banAge = TimeUnit.HOURS.toSeconds(val);
                break;
            case 3:
                banAge = TimeUnit.DAYS.toSeconds(val);
                break;
        }

        ChatTask task = chatTasks.getTask(pType);
        task.mBanTimeSec = banAge;
        chatTasks.saveTask(task);

        return banAge;
    }


    SeekBar.OnSeekBarChangeListener onSeekBarBanCountListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                ChatTask.TYPE type = ChatTask.TYPE.valueOf(seekBar.getTag().toString());
                String text = progress + "";
                switch (type) {
                    case STICKERS:
                        tvBanSticksWarningCount.setText(text);
                        break;
                    case LINKS:
                        tvBanLinksWarningCount.setText(text);
                        break;
                    case VOICE:
                        tvBanVoiceWarningCount.setText(text);
                        break;
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            ChatTask.TYPE type = ChatTask.TYPE.valueOf(seekBar.getTag().toString());
            int val = seekBar.getProgress();
            ChatTask task = chatTasks.getTask(type);
            task.mAllowCountPerUser = val;
            chatTasks.saveTask(task);

        }
    };


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tvBannedWordsCount:
                    startActivity(new Intent(mContext, ActivityBannedWordsList.class).putExtra("chat_id", chatId));

                    break;
                case R.id.tvChatUsername:
                    if (isAdmin)
                        dialogSetUsername(chatUsername);
                    else
                        MyToast.toast(mContext, R.string.toast_access_denied_admin);
                    break;
                case R.id.btnConvertToSuper:
                    confirmConvertToSuperGroup();
                    break;
                case R.id.tvTitle:
                    dialogRenameChatTitle();
                    break;
                case R.id.tvMainAdmin:
                    if (adminId == 0)
                        return;
                    break;
                case R.id.tvChatDescription:
                    dialogEditChatDescription(null);
                    break;
                case R.id.chkAnyoneInviteFriendsSuper:
                    switchAnyoneCanInviteSuperGroup();
                    break;
                case R.id.chkAnyoneManageGroup:
                    switchAnyoneManageGroup();
                    break;

                case R.id.chkBlackListedWordsEnabled:
                    ChatTask task = chatTasks.getTask(ChatTask.TYPE.BANWORDS);
                    task.isEnabled = chkBlackListedWordsEnabled.isChecked();
                    layerBanForBlackWord.setVisibility(task.isEnabled ? View.VISIBLE : View.GONE);
                    chatTasks.saveTask(task);
                    break;


                // Link tasks:
                case R.id.chkBanForLinks:
                    boolean isBan = chkBanForLinks.isChecked();
                    setBanEnabledForTask(ChatTask.TYPE.LINKS, isBan);
                    chkReturnOnBanLinksExpired.setEnabled(isBan && chatTasks.getTask(ChatTask.TYPE.LINKS).mBanTimeSec > 0);
                    layerBanLinksAge.setVisibility(isBan ? View.VISIBLE : View.GONE);
                    chkReturnOnBanLinksExpired.setVisibility(isBan ? View.VISIBLE : View.GONE);

                    break;
                case R.id.chkRemoveLinks:
                    if (TgUtils.isGroup(chatType)) {
                        MyToast.toast(mContext, R.string.toast_deletion_not_avail);
                        chkRemoveLinks.setChecked(false);
                        return;
                    }

                    task = chatTasks.getTask(ChatTask.TYPE.LINKS);
                    task.isRemoveMessage = chkRemoveLinks.isChecked();
                    chatTasks.saveTask(task);
                    break;
                case R.id.chkReturnOnBanLinksExpired:
                    task = chatTasks.getTask(ChatTask.TYPE.LINKS);
                    task.isReturnOnBanExpired = chkReturnOnBanLinksExpired.isChecked();
                    chatTasks.saveTask(task);
                    break;

                //Stickers tasks:
                case R.id.chkRemoveStickers:
                    if (TgUtils.isGroup(chatType)) {
                        MyToast.toast(mContext, R.string.toast_deletion_not_avail);
                        chkRemoveStickers.setChecked(false);
                        return;
                    }

                    task = chatTasks.getTask(ChatTask.TYPE.STICKERS);
                    task.isRemoveMessage = chkRemoveStickers.isChecked();
                    chatTasks.saveTask(task);
                    break;

                case R.id.chkBanForSticks:
                    isBan = chkBanForSticks.isChecked();
                    setBanEnabledForTask(ChatTask.TYPE.STICKERS, isBan);

                    layerBanSticksTime.setVisibility(isBan ? View.VISIBLE : View.GONE);
                    chkReturnOnBanSticksExpired.setVisibility(layerBanSticksTime.getVisibility());
                    chkReturnOnBanSticksExpired.setEnabled(isBan && chatTasks.getTask(ChatTask.TYPE.STICKERS).mBanTimeSec > 0);
                    break;
                case R.id.chkReturnOnBanSticksExpired:
                    task = chatTasks.getTask(ChatTask.TYPE.STICKERS);
                    task.isReturnOnBanExpired = chkReturnOnBanSticksExpired.isChecked();
                    chatTasks.saveTask(task);
                    break;

                case R.id.tvInviteLink:
                    if (isAdmin)
                        popupInviteLink();
                    else
                        MyToast.toast(mContext, R.string.toast_access_denied_admin);
                    break;
                case R.id.tvAdminsCount:
                    startActivity(new Intent(mContext, ActivityChatUsers.class)
                            .putExtra("filter", "admins")
                            .putExtra("type", chatType)
                            .putExtra("chat_id", chatId)
                            .putExtra("channel_id", channelId)
                            .putExtra("title", chatTitle)
                            .putExtra("group_id", groupId)
                    );
                    break;
                case R.id.tvUsersCount:
                    startActivity(new Intent(mContext, ActivityChatUsers.class)
                            .putExtra("type", chatType)
                            .putExtra("chat_id", chatId)
                            .putExtra("channel_id", channelId)
                            .putExtra("title", chatTitle)
                            .putExtra("group_id", groupId)
                    );
                    break;


                case R.id.tvKickedCount:
                    if (TgUtils.isSuperGroup(chatType)) {
                        startActivity(new Intent(mContext, ActivityBanList.class)
                                .putExtra("type", chatType)
                                .putExtra("chat_id", chatId)
                                .putExtra("channel_id", channelId));
                    } else {
                        startActivity(new Intent(mContext, ActivityBanList.class)
                                .putExtra("type", chatType)
                                .putExtra("chat_id", chatId));
                    }
                    break;
                case R.id.chkReturnOnBannedWordsExpired:
                    task = chatTasks.getTask(ChatTask.TYPE.BANWORDS);
                    task.isReturnOnBanExpired = chkReturnOnBannedWordsExpired.isChecked();
                    chatTasks.saveTask(task);
                    break;

                case R.id.chkRemoveLeaveMsg:
                    if (TgUtils.isGroup(chatType)) {
                        MyToast.toast(mContext, R.string.toast_deletion_not_avail);
                        chkRemoveLeaveMsg.setChecked(false);
                        return;
                    }

//                    antiSpamRule = getAntispamRule();
//                    antiSpamRule.isRemoveLeaveMessage = chkRemoveLeaveMsg.isChecked();
//                    saveAntispamRule(antiSpamRule);

                    task = chatTasks.getTask(ChatTask.TYPE.LeaveMsg);
                    task.isEnabled = chkRemoveJoinedMsg.isChecked();
                    chatTasks.saveTask(task);
                    if (task.isEnabled)
                        ServiceChatTask.start(mContext);
                    break;

                case R.id.chkRemoveJoinedMsg:
                    if (TgUtils.isGroup(chatType)) {
                        MyToast.toast(mContext, R.string.toast_deletion_not_avail);
                        chkRemoveJoinedMsg.setChecked(false);
                        return;
                    }

                    task = chatTasks.getTask(ChatTask.TYPE.JoinMsg);
                    task.isEnabled = chkRemoveJoinedMsg.isChecked();
                    chatTasks.saveTask(task);

//                    antiSpamRule = getAntispamRule();
//                    antiSpamRule.isRemoveJoinMessage = chkRemoveJoinedMsg.isChecked();
//                    saveAntispamRule(antiSpamRule);
                    if (task.isEnabled)
                        ServiceChatTask.start(mContext);
                    break;
                case R.id.tvBanLinksAllowCount:
                    setTaskWarnCountDialog(ChatTask.TYPE.LINKS, seekBarBanLinksCount, tvBanLinksWarningCount);
                    break;
                case R.id.tvBanSticksWarningCount:
                    setTaskWarnCountDialog(ChatTask.TYPE.STICKERS, seekBarBanSticksCount, tvBanSticksWarningCount);
                    break;

                case R.id.tvBanWordsAllowCount:
                    setTaskWarnCountDialog(ChatTask.TYPE.BANWORDS, null, tvBanWordsAllowCount);
                    break;
                case R.id.tvBtnWarnLinksFreq:
                    showPopupForFrequencyTask(ChatTask.TYPE.LINKS, tvBtnWarnLinksFreq);
                    break;

                case R.id.tvBtnWarnStickersFreq:
                    showPopupForFrequencyTask(ChatTask.TYPE.STICKERS, tvBtnWarnStickersFreq);
                    break;
                case R.id.tvBtnWarnBanWordsFreq:
                    showPopupForFrequencyTask(ChatTask.TYPE.BANWORDS, tvBtnWarnBanWordsFreq);
                    break;
                case R.id.chkRemoveVoices:
                    if (TgUtils.isGroup(chatType)) {
                        MyToast.toast(mContext, R.string.toast_deletion_not_avail);
                        chkRemoveVoices.setChecked(false);
                        return;
                    }

                    task = chatTasks.getTask(ChatTask.TYPE.VOICE);
                    task.isRemoveMessage = chkRemoveVoices.isChecked();
                    chatTasks.saveTask(task);
                    break;
                case R.id.chkBanForVoice:
                    task = chatTasks.getTask(ChatTask.TYPE.VOICE);
                    task.isBanUser = chkBanForVoice.isChecked();
                    chatTasks.saveTask(task);

                    layerBanVoiceTime.setVisibility(task.isBanUser ? View.VISIBLE : View.GONE);
                    chkReturnOnBanVoiceExpired.setVisibility(layerBanSticksTime.getVisibility());
                    chkReturnOnBanVoiceExpired.setEnabled(task.isBanUser && chatTasks.getTask(ChatTask.TYPE.STICKERS).mBanTimeSec > 0);
                    break;

                case R.id.tvBtnWarnVoiceFreq:
                    showPopupForFrequencyTask(ChatTask.TYPE.VOICE, (TextView) v);
                    break;
                case R.id.chkReturnOnBanVoiceExpired:
                    task = chatTasks.getTask(ChatTask.TYPE.VOICE);
                    task.isReturnOnBanExpired = chkReturnOnBanVoiceExpired.isChecked();
                    chatTasks.saveTask(task);
                    break;
                case R.id.tvBanVoiceWarningCount:
                    setTaskWarnCountDialog(ChatTask.TYPE.VOICE, seekBarBanVoiceAllowCount, tvBanVoiceWarningCount);
                    break;

                //Flood Control view:
                case R.id.chkFloodControlEnabled:
                    task = chatTasks.getTask(ChatTask.TYPE.FLOOD);
                    task.isEnabled = chkFloodControlEnabled.isChecked();
                    chatTasks.saveTask(task);
                    layerFloodControl.setVisibility(task.isEnabled ? View.VISIBLE : View.GONE);
                    break;
                case R.id.tvFloodMsgAllowCount:
                    setTaskWarnCountDialog(ChatTask.TYPE.FLOOD, null, tvFloodMsgAllowCount);
                    break;
                case R.id.chkBanFloodUser:
                    task = chatTasks.getTask(ChatTask.TYPE.FLOOD);
                    task.isBanUser = chkBanFloodUser.isChecked();
                    chatTasks.saveTask(task);

                    boolean b = task.isBanUser && spinnerBanFloodTime.getSelectedItemPosition() > 0;
                    chkReturnOnBanFloodExpired.setEnabled(b);
                    edtFloodBanTimeVal.setEnabled(b);
                    spinnerBanFloodTime.setEnabled(b);


                    break;
                case R.id.chkReturnOnBanFloodExpired:
                    task = chatTasks.getTask(ChatTask.TYPE.FLOOD);
                    task.isReturnOnBanExpired = chkReturnOnBanFloodExpired.isChecked();
                    chatTasks.saveTask(task);
                    break;
                case R.id.tvBtnWarnBanFloodFreq:
                    showPopupForFrequencyTask(ChatTask.TYPE.FLOOD, (TextView) v);
                    break;
                case R.id.tvNoticePhoneBookEnabled:
                    startActivity(new Intent(mContext, ActivitySettings.class));
                    break;

                case R.id.btnEditCommands:
                    Intent intent = new Intent(mContext, ActivityChatCommands.class).putExtra("chat_id", chatId);
                    startActivityForResult(intent, Const.ACTION_REFRESH_COMMANDS);
                    break;

                case R.id.chkCommandsEnable:
                    task = chatTasks.getTask(ChatTask.TYPE.COMMAND);
                    task.isEnabled = chkCommandsEnable.isChecked();
                    btnEditCommands.setEnabled(task.isEnabled);
                    chatTasks.saveTask(task);
                    break;

            }
        }
    };


    void setBanEnabledForTask(ChatTask.TYPE pType, boolean isBan) {
        ChatTask task = chatTasks.getTask(pType);
        task.isBanUser = isBan;
        chatTasks.saveTask(task);
    }


    private void showPopupForFrequencyTask(final ChatTask.TYPE pType, final TextView tvPressedTextView) {
        View popupView = UIUtils.inflate(mContext, R.layout.popup_warnuser_frequency);
        final PopupWindow popupWindow = new PopupWindow(
                popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Button btnClose = UIUtils.getView(popupView, R.id.btnClose);
        Button btnSave = UIUtils.getView(popupView, R.id.btnSave);
        final EditText edtFirstWarning = UIUtils.getView(popupView, R.id.edtWarningFirst);
        final EditText edtLastWarning = UIUtils.getView(popupView, R.id.edtWarningLast);
        TextView tvHelpWarnFormat = UIUtils.getViewT(popupView, R.id.tvHelpWarnFormat);

        mCurrentPopupWindow = popupWindow;

        final RadioGroup radioGroup = UIUtils.getView(popupView, R.id.rgWarnFrequency);
        final ChatTask task = chatTasks.getTask(pType);

        edtFirstWarning.setText(task.mWarnTextFirst);
        edtLastWarning.setText(task.mWarnTextLast);

        if (pType == ChatTask.TYPE.FLOOD) {
            popupView.findViewById(R.id.rbWarnFreqEvery).setVisibility(View.GONE);
            popupView.findViewById(R.id.rbWarnFreqFirstMsg).setVisibility(View.GONE);
            popupView.findViewById(R.id.rbWarnFreqFirstLast).setVisibility(View.GONE);
            popupView.findViewById(R.id.rbWarnFreqSecondLast).setVisibility(View.GONE);
            if (task.mWarnFrequency == 3) //Flood control have only "last message warn" and "dont warn"
                task.mWarnFrequency = 1;
        }

        int checkedRbID = 0;
        switch (task.mWarnFrequency) {
            case 0:
                checkedRbID = R.id.rbWarnFreqNone;
                UIUtils.setBatchVisibility(View.GONE, edtFirstWarning, edtLastWarning);
                break;
            case 1:
                checkedRbID = R.id.rbWarnFreqLastMsg;
                edtFirstWarning.setVisibility(View.GONE);
                edtLastWarning.setVisibility(View.VISIBLE);
                break;
            case 2:
                checkedRbID = R.id.rbWarnFreqFirstMsg;
                edtFirstWarning.setVisibility(View.GONE);
                edtLastWarning.setVisibility(View.VISIBLE);
                break;
            case 3:
                checkedRbID = R.id.rbWarnFreqFirstLast;
                edtFirstWarning.setVisibility(View.VISIBLE);
                edtLastWarning.setVisibility(View.VISIBLE);
                break;
            case 4:
                checkedRbID = R.id.rbWarnFreqSecondLast;
                edtFirstWarning.setVisibility(View.VISIBLE);
                edtLastWarning.setVisibility(View.VISIBLE);
                break;
            case 5:
                checkedRbID = R.id.rbWarnFreqEvery;
                edtFirstWarning.setVisibility(View.VISIBLE);
                edtLastWarning.setVisibility(View.GONE);
                break;
        }
        RadioButton rbChecked = UIUtils.getView(popupView, checkedRbID);
        rbChecked.setChecked(true);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbWarnFreqNone:
                        UIUtils.setBatchVisibility(View.GONE, edtFirstWarning, edtLastWarning);
                        break;
                    case R.id.rbWarnFreqLastMsg:
                        edtFirstWarning.setVisibility(View.GONE);
                        edtLastWarning.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rbWarnFreqFirstMsg:
                        edtFirstWarning.setVisibility(View.GONE);
                        edtLastWarning.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rbWarnFreqFirstLast:
                        edtFirstWarning.setVisibility(View.VISIBLE);
                        edtLastWarning.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rbWarnFreqSecondLast:
                        edtFirstWarning.setVisibility(View.VISIBLE);
                        edtLastWarning.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rbWarnFreqEvery:
                        edtFirstWarning.setVisibility(View.VISIBLE);
                        edtLastWarning.setVisibility(View.GONE);
                        break;
                }
            }
        });

        popupWindow.showAtLocation(tvPressedTextView, Gravity.CENTER, 0, 0);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mCurrentPopupWindow = null;
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.update();

        setEditWarnTextWatcher(edtFirstWarning, pType, true);
        setEditWarnTextWatcher(edtLastWarning, pType, false);

        View.OnClickListener onclick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnClose:
                        popupWindow.dismiss();
                        break;
                    case R.id.btnSave:
                        int resSelectedRadionID = radioGroup.getCheckedRadioButtonId();
                        switch (resSelectedRadionID) {
                            case R.id.rbWarnFreqNone:
                                saveWarnFrequencyTask(pType, 0);
                                break;
                            case R.id.rbWarnFreqLastMsg:
                                saveWarnFrequencyTask(pType, 1);
                                break;
                            case R.id.rbWarnFreqFirstMsg:
                                saveWarnFrequencyTask(pType, 2);
                                break;
                            case R.id.rbWarnFreqFirstLast:
                                saveWarnFrequencyTask(pType, 3);
                                break;
                            case R.id.rbWarnFreqSecondLast:
                                saveWarnFrequencyTask(pType, 4);
                                break;
                            case R.id.rbWarnFreqEvery:
                                saveWarnFrequencyTask(pType, 5);
                                break;
                        }

                        if (resSelectedRadionID > -1) {
                            RadioButton rbSelected = UIUtils.getView(radioGroup, resSelectedRadionID);
                            tvPressedTextView.setText(rbSelected.getText());
                        }
                        popupWindow.dismiss();

                        break;
                    case R.id.tvHelpWarnFormat:
                        View view = UIUtils.inflate(mContext, R.layout.layout_dialog_help_warning_formattin);
                        TextView tvHelpText = UIUtils.getView(view, R.id.tvHelpText);

                        String text = getString(R.string.text_help_formatting);

                        tvHelpText.setText(Html.fromHtml(text.replaceAll("(\r\n|\n)", "<br />")));

                        new AlertDialog.Builder(mContext)
                                .setTitle("Formattin help")
                                .setView(view)
                                //"\nFormatting:\n" +
                                //"You can user tilda ` symbol for highlite text")
                                .setPositiveButton("Ok", null)
                                .show();
                        break;

                }
            }
        };
        UIUtils.setBatchClickListener(onclick, btnSave, btnClose, tvHelpWarnFormat);
    }

    void setEditWarnTextWatcher(final EditText editText, final ChatTask.TYPE pType, final boolean isFirstWarn) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                editText.setTag(System.currentTimeMillis()); //TODO ingore when writing default value? не нужно
            }

            @Override
            public void afterTextChanged(final Editable s) {
                editText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        long ts = Long.valueOf(editText.getTag().toString());
                        if (System.currentTimeMillis() - ts < 1400) return;

                        String text = s.toString().trim();
                        ChatTask task = chatTasks.getTask(pType);
                        if (isFirstWarn)
                            task.mWarnTextFirst = text;
                        else
                            task.mWarnTextLast = text;
                        DBHelper.getInstance().saveWarnText(text, isFirstWarn, task.id);
                        //chatTasks.saveTask(task);
                        editText.getBackground().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

                    }
                }, 1500);
                editText.getBackground().setColorFilter(getResources().getColor(R.color.md_teal_500), PorterDuff.Mode.SRC_ATOP);
            }
        });

    }

    private void saveWarnFrequencyTask(ChatTask.TYPE pType, int freqType) {
        ChatTask task = chatTasks.getTask(pType);
        task.mWarnFrequency = freqType;
        chatTasks.saveTask(task);
    }


    private void setTaskWarnCountDialog(ChatTask.TYPE pType, @Nullable final SeekBar skToChange, final TextView tvValue) {
        final ChatTask task = chatTasks.getTask(pType);
        new DialogInputValue(mContext)
                .setValue(task.mAllowCountPerUser)
                .onApply(new DialogInputValue.SetValueCallback() {
                    @Override
                    public void onSetValue(int value) {
                        tvValue.setText(value + "");
                        task.mAllowCountPerUser = value;
                        if (skToChange != null)
                            skToChange.setProgress(value);
                        chatTasks.saveTask(task);
                    }
                })
                .showDialog();
    }


//    private void saveAntispamRule(AntiSpamRule antiSpamRule) {
//        DBHelper.getInstance().setAntiSpamRule(chatId, antiSpamRule);
//        if (antiSpamRule.isBanForLinks || antiSpamRule.isBanForStickers || antiSpamRule.isRemoveLinks || antiSpamRule.isRemoveStickers)
//            ServiceAntispam.start(mContext);
//    }

    private void popupInviteLink() {
        PopupMenu popup = new PopupMenu(mContext, tvInviteLink);
        Menu menu = popup.getMenu();
        if (chatInviteLink == null || chatInviteLink.isEmpty()) {
            menu.add(0, 1, 0, R.string.generate_invite_link);
        } else {
            menu.add(0, 1, 0, R.string.generate_new_invite_link);
            menu.add(0, 2, 0, R.string.action_copy_link);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1:
                        if (chatInviteLink == null || chatInviteLink.isEmpty()) {
                            generateInviteLink();
                        } else {
                            dialogConfirmRevokeInviteLink();
                        }
                        break;
                    case 2:
                        Utils.copyToClipboard(chatInviteLink, mContext);
                        MyToast.toast(mContext, R.string.toast_copied_to_clipboard);
                        break;
                }
                return false;
            }
        });

        popup.show();
    }

    void dialogConfirmRevokeInviteLink() {
        new AlertDialog.Builder(mContext)
                .setTitle("Confirm")
                .setMessage(R.string.message_confirm_revoke_link)
                .setNegativeButton(R.string.btnCancel, null)
                .setPositiveButton(R.string.btnContinue, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        generateInviteLink();
                    }
                })
                .show();
    }

    void generateInviteLink() {
        TdApi.TLFunction f = new TdApi.ExportChatInviteLink(chatId);
        TgH.sendOnUi(f, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                if (TgUtils.isError(object)) {
                    MyToast.toast(mContext, object.toString());
                } else {
                    TdApi.ChatInviteLink inviteLink = (TdApi.ChatInviteLink) object;
                    chatInviteLink = inviteLink.inviteLink;
                    tvInviteLink.setText(chatInviteLink);
                    MyToast.toast(mContext, "Invite link created");
                }
            }
        });
    }

    /*
    private AntiSpamRule getAntispamRule() {
        AntiSpamRule antiSpamRule = DBHelper.getInstance().getAntispamRule(chatId);
        if (antiSpamRule == null) antiSpamRule = new AntiSpamRule();
        return antiSpamRule;
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 0, R.string.changeChatAbout);
        if (isAdmin)
            menu.add(0, 3, 0, R.string.actionDeleteGroup);

        if (!isChannel)
            menu.add(0, 2, 0, R.string.action_reset_warns);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                dialogEditChatDescription(null);
                break;
            case 2:
                DBHelper.getInstance().resetWarnCountForChat(chatId);
                MyToast.toast(mContext, "Reset done");
                break;
            case 3:
                dialogConfirmDeleteGroup();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // menu.findItem(3).setVisible(isAdmin);
        return super.onPrepareOptionsMenu(menu);
    }

    /*
    ===== methods
     */

    void dialogRenameChatTitle() {
        View v = UIUtils.inflate(mContext, R.layout.dialog_chat_title);
        final EmojiconEditText editText = UIUtils.getView(v, R.id.editInputTitle);
        editText.setText(chatTitle);
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.chat_title)
                .setView(v)
                .setNegativeButton(R.string.btnCancel, null)
                .setPositiveButton(R.string.action_rename, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String newTitle = editText.getText().toString().trim();
                        if (newTitle.isEmpty()) {
                            MyToast.toast(mContext, R.string.error_empty_chat_title);
                            dialogRenameChatTitle();
                            return;
                        }
                        TdApi.TLFunction f = new TdApi.ChangeChatTitle(chatId, newTitle);
                        TgH.sendOnUi(f, new Client.ResultHandler() {
                            @Override
                            public void onResult(TdApi.TLObject object) {
                                if (TgUtils.isError(object))
                                    MyToast.toast(mContext, "Error:\n" + object.toString());
                                else {
                                    chatTitle = newTitle;
                                    tvTitle.setText(newTitle);
                                }
                            }
                        });
                    }
                })
                .show();
    }

    void dialogConfirmDeleteGroup() {
        LinearLayout view = new LinearLayout(mContext);
        view.setOrientation(LinearLayout.VERTICAL);
        TextView t = new TextView(this);
        t.setText(getString(R.string.text_confirm_deletion_group) + "\n" + chatTitle);
        view.setPadding(16, 8, 16, 8);
        view.addView(t);
        final CheckBox c = new CheckBox(this);
        c.setText(R.string.text_delete_group);
        view.addView(c);
        final AlertDialog d = new AlertDialog.Builder(mContext)
                .setView(view)
                .setTitle(R.string.title_delete_group)
                .setPositiveButton(R.string.btnCancel, null)
                .setNegativeButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialogConfirmDelete2();
                    }
                })
                .show();
        d.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
        c.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = c.isChecked();
                d.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(enabled);
            }
        });
    }

    private void dialogConfirmDelete2() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.title_delete_group)
                .setMessage(getString(R.string.text_confirm_deletion_group))
                .setPositiveButton(R.string.btnCancel, null)
                .setNegativeButton(R.string.button_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TdApi.TLFunction f;
                        if (TgUtils.isGroup(chatType)) {
                            f = new TdApi.DeleteChatHistory(chatId, false);
                            final TdApi.TLFunction ff = new TdApi.ChangeChatMemberStatus(chatId, TgH.selfProfileId, new TdApi.ChatMemberStatusLeft());
                            TgH.send(ff);
                        } else {
                            f = new TdApi.DeleteChannel(channelId);
                        }
                        TgH.send(f, new Client.ResultHandler() {
                            @Override
                            public void onResult(TdApi.TLObject object) {
                                MyToast.toast(mContext, R.string.toast_group_deleted);
                                DBHelper.getInstance().deleteGroup(chatId);
                                finish();
                            }
                        });
                    }
                })
                .show();
    }

    private void dialogEditChatDescription(@Nullable String defTitle) {
        View v = UIUtils.inflate(mContext, R.layout.dialog_chat_edit_about);
        final EmojiconEditText editInputDescription = UIUtils.getView(v, R.id.editInputDescription);
        editInputDescription.setText(chatDescription);
        if (defTitle != null)
            editInputDescription.setText(defTitle);
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.titleEditChatDescription)
                .setView(v)
                .setNegativeButton(R.string.btnCancel, null)
                .setPositiveButton(R.string.btnSave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String newChatDescription = editInputDescription.getText().toString().trim();
                        TdApi.TLFunction f = new TdApi.ChangeChannelAbout(channelId, newChatDescription);
                        TgH.sendOnUi(f, new Client.ResultHandler() {
                            @Override
                            public void onResult(TdApi.TLObject object) {
                                if (TgUtils.isError(object)) {
                                    MyToast.toast(mContext, "Error:\n" + object.toString());
                                    dialogSetUsername(newChatDescription);
                                } else {
                                    chatDescription = newChatDescription;
                                    tvChatDescription.setText(newChatDescription);
                                }
                            }
                        });
                    }
                })
                .show();
    }

    private void getChatInfo() {
        TdApi.TLFunction f = new TdApi.GetChat(chatId);
        TgH.sendOnUi(f, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                if (TgUtils.isError(object)) {
                    return;
                }
                TdApi.Chat chat = (TdApi.Chat) object;
                TgImageGetter tgImageGetter = new TgImageGetter();
                if (chat.photo != null)
                    tgImageGetter.setImageToView(btnAva, chat.photo.small);
            }
        });

        if (TgUtils.isGroup(chatType)) {
            getGroupInfo();
        } else {
            getSuperGroupInfo();
        }
    }

    private void getGroupInfo() {
        TdApi.TLFunction f = new TdApi.GetGroupFull(groupId);
        TgH.send(f, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                // MyLog.log(object.toString());
                if (TgUtils.isError(object)) {
                    TdApi.Error error = (TdApi.Error) object;
                    if (error.code == 6 && !isFinishing()) {
                        Utils.sleep(2000); // some sleep and try again, maybe chats will loading later
                        getGroupInfo();
                    } else {
                        MyToast.toast(mContext, "Error: " + error.message);
                        onUiThread(hideLoading);
                    }
                    return;
                }

                TdApi.GroupFull groupFull = (TdApi.GroupFull) object;
                ActivityGroupInfo.this.groupFull = groupFull;
                chatUsersCount = groupFull.group.memberCount;
                isAdmin = groupFull.group.status.getConstructor() == TdApi.ChatMemberStatusCreator.CONSTRUCTOR;
                adminId = groupFull.creatorUserId;
                adminsCount = 0;
                chatInviteLink = groupFull.inviteLink;
                groupAnyoneCanEdit = groupFull.group.anyoneCanEdit;
                for (TdApi.ChatMember member : groupFull.members) {
                    if (member.userId == adminId) {
                        if (TgUtils.isUserPrivileged(member.status.getConstructor()))
                            adminsCount++;
                        TdApi.User user = TgUtils.getUser(adminId);
                        adminName = user.firstName + " " + user.lastName;
                    }
                }

                onUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onGroupInfoLoaded();
                    }
                });
            }
        });
    }

    void switchAnyoneCanInviteSuperGroup() {
        if (!isAdmin) {
            MyToast.toast(mContext, R.string.toast_access_denied_admin);
            chkAnyoneInviteFriendsSuper.setChecked(superAanyoneCanInvite);
            return;
        }
        final ProgressBar prgChangeState = getView(R.id.prgChangeState);
        prgChangeState.setVisibility(View.VISIBLE);
        chkAnyoneInviteFriendsSuper.setEnabled(false);

        boolean canInvite = chkAnyoneInviteFriendsSuper.isChecked();
        TdApi.TLFunction f = new TdApi.ToggleChannelInvites(channelId, canInvite);
        TgH.sendOnUi(f, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                if (object.getConstructor() == TdApi.Channel.CONSTRUCTOR) {
                    TdApi.Channel channel = (TdApi.Channel) object;
                    boolean canInvite = channel.anyoneCanInvite;
                    MyToast.toast(mContext, "Anyone can invite: " + (canInvite ? "True" : "False"));
                } else
                    MyToast.toast(mContext, object);
                prgChangeState.setVisibility(View.GONE);
                chkAnyoneInviteFriendsSuper.setEnabled(true);
                //p.dismiss();
            }
        });
    }

    void switchAnyoneManageGroup() {
        if (!isAdmin) {
            MyToast.toast(mContext, R.string.toast_access_denied_admin);
            return;
        }

        final ProgressBar prgChangeState = getView(R.id.prgChangeState);
        prgChangeState.setVisibility(View.VISIBLE);
        chkAnyoneManageGroup.setEnabled(false);

        boolean canInvite = chkAnyoneManageGroup.isChecked();
        TdApi.TLFunction f = new TdApi.ToggleGroupEditors(groupId, canInvite);
        TgH.sendOnUi(f, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                if (object.getConstructor() == TdApi.Group.CONSTRUCTOR)
                    MyToast.toast(mContext, "Changes saved");
                else
                    MyToast.toast(mContext, "Error:\n" + object.toString());
                //p.dismiss();
                prgChangeState.setVisibility(View.GONE);
                chkAnyoneManageGroup.setEnabled(true);
            }
        });
    }

    Runnable hideLoading = new Runnable() {
        @Override
        public void run() {
            viewLoading.setVisibility(View.GONE);
        }
    };

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

    private void getSuperGroupInfo() {
        TdApi.TLFunction f = new TdApi.GetChannelFull(channelId);
        TgH.send(f, new Client.ResultHandler() {
            @Override
            public void onResult(final TdApi.TLObject object) {
                // MyLog.log(object.toString());
                if (TgUtils.isError(object)) {
                    TdApi.Error error = (TdApi.Error) object;
                    if (error.code == 6 && !isFinishing()) {
                        Utils.sleep(2000); // some sleep and try again, maybe chats will loading later
                        getSuperGroupInfo();
                    } else {
                        MyToast.toast(mContext, "Error: " + error.message);
                        onUiThread(hideLoading);
                    }
                    return;
                }

                onUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TdApi.ChannelFull channelFull = (TdApi.ChannelFull) object;
                        chatInviteLink = channelFull.inviteLink;
                        chatDescription = channelFull.about;
                        chatUsersCount = channelFull.memberCount;
                        adminsCount = channelFull.administratorCount;
                        kickedCount = channelFull.kickedCount;
                        chatUsername = channelFull.channel.username;
                        isAdmin = channelFull.channel.status.getConstructor() == TdApi.ChatMemberStatusCreator.CONSTRUCTOR;
                        superAanyoneCanInvite = channelFull.channel.anyoneCanInvite;
                        if (isAdmin) {
                            adminId = TgH.selfProfileId;
                            adminName = "@" + TgH.selfProfileUsername;
                            onSuperGroupInfoLoaded();
                        } else
                            getSuperAdmins();

                        if (!channelFull.channel.isSupergroup) {// channel
                            isChannel = true;
                            findViewById(R.id.layerAutoban).setVisibility(View.GONE);
                            findViewById(R.id.viewOther).setVisibility(View.GONE);
                            findViewById(R.id.viewWelcomeText).setVisibility(View.GONE);
                            findViewById(R.id.viewCommands).setVisibility(View.GONE);

                            tvKickedCount.setVisibility(View.GONE);
                            chkAnyoneInviteFriendsSuper.setVisibility(View.GONE);
                            setTitle(R.string.action_manage_channel);
                            invalidateOptionsMenu();
                            tvChatType.setText(R.string.chatTypeChannel);
                        } else {
                            tvChatType.setText(R.string.chatTypeSuperGroup);
                        }
                    }
                });
            }
        });
    }

    private void getSuperAdmins() {
        TdApi.TLFunction f = new TdApi.GetChannelMembers(channelId, new TdApi.ChannelMembersAdministrators(), 0, 100);
        TgH.send(f, new Client.ResultHandler() {
            @Override
            public void onResult(TdApi.TLObject object) {
                if (object.getConstructor() == TdApi.ChatMembers.CONSTRUCTOR) {
                    TdApi.ChatMembers users = (TdApi.ChatMembers) object;
                    for (final TdApi.ChatMember chatMember : users.members) {
                        if (chatMember.status.getConstructor() == TdApi.ChatMemberStatusCreator.CONSTRUCTOR) {
                            adminId = chatMember.userId;

                            TgH.send(new TdApi.GetChatMember(channelId, chatMember.userId), new Client.ResultHandler() {
                                @Override
                                public void onResult(TdApi.TLObject object) {
                                    TdApi.User admin = TgUtils.getUser(chatMember);
                                    adminName = admin.firstName + " " + admin.lastName;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            onSuperGroupInfoLoaded();
                                        }
                                    });
                                }
                            });
                            return;
                        }
                    }
                } else {
                    adminName = "";
                    onUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onSuperGroupInfoLoaded();
                        }
                    });
                }
            }
        });
    }

    private void onGroupInfoLoaded() {
        tvChatDescription.setVisibility(View.GONE);
        if (chatInviteLink.isEmpty())
            tvInviteLink.setText(R.string.hint_invite_link_not_created);
        else
            tvInviteLink.setText(chatInviteLink);


        tvUsersCount.setText(getString(R.string.participiantsCount) + " " + chatUsersCount);
        tvKickedCount.setText(getString(R.string.kickedCount) + " " + DBHelper.getInstance().getBannedCount(chatId));

        tvAdminsCount.setText(getString(R.string.adminsCount) + " " + adminsCount);

        viewLoading.setVisibility(View.GONE);
        viewContent.setVisibility(View.VISIBLE);
        tvChatAdmin.setText(adminName);
        chkAnyoneManageGroup.setChecked(groupAnyoneCanEdit);
        invalidateOptionsMenu();
    }


    private void onSuperGroupInfoLoaded() {
        tvUsersCount.setText(getString(R.string.participiantsCount) + " " + chatUsersCount);
        tvKickedCount.setText(getString(R.string.kickedCount) + " " + kickedCount);
        tvAdminsCount.setText(getString(R.string.adminsCount) + " " + adminsCount);
        tvChatDescription.setText(chatDescription);
        chkAnyoneInviteFriendsSuper.setChecked(superAanyoneCanInvite);
        if (chatDescription.isEmpty()) {
            tvChatDescription.setText(R.string.hint_no_chat_about);
            UIUtils.setTextColotRes(tvChatDescription, R.color.md_grey_500);
        }

        if (chatInviteLink.isEmpty())
            tvInviteLink.setText(R.string.hint_invite_link_not_created);
        else
            tvInviteLink.setText(chatInviteLink);

        if (chatUsername.isEmpty())
            tvChatUsername.setText(R.string.hint_short_link_not_created);
        else
            tvChatUsername.setText("@" + chatUsername);

        viewLoading.setVisibility(View.GONE);
        viewContent.setVisibility(View.VISIBLE);
        tvChatAdmin.setText(adminName);
        invalidateOptionsMenu();
    }

    void dialogSetUsername(@Nullable final String defName) {
        View v = UIUtils.inflate(mContext, R.layout.dialog_set_username);
        final EditText edtInputUsername = UIUtils.getView(v, R.id.edtInputUsername);
        final TextView tvValueHint = UIUtils.getView(v, R.id.tvValueHint);
        if (defName != null) edtInputUsername.setText(defName);
        tvValueHint.setText("");
        AlertDialog d = new AlertDialog.Builder(this)
                .setView(v)
                .setTitle(R.string.dialog_title_edit_link)
                .setNegativeButton(R.string.btnCancel, null)
                .setPositiveButton(R.string.btnSave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String s = edtInputUsername.getText().toString().trim().replace("@", "");
                        TdApi.TLFunction f = new TdApi.ChangeChannelUsername(channelId, s);
                        TgH.TG().send(f, new Client.ResultHandler() {
                            @Override
                            public void onResult(TdApi.TLObject object) {
                                if (!TgUtils.isError(object)) {
                                    tvChatUsername.setText(s);
                                    MyToast.toast(mContext, R.string.toast_chat_link_added);
                                } else {
                                    TdApi.Error er = (TdApi.Error) object;
                                    if (er.code == 400) {
                                        if (er.message.equals("CHANNELS_ADMIN_PUBLIC_TOO_MUCH")) {
                                            MyToast.toast(mContext, "Error code 400:\n" + getString(R.string.error_too_many_public_links));
                                            return;
                                        }
                                        if (er.message.equals("USERNAME_OCCUPIED")) {
                                            MyToast.toastL(mContext, getString(R.string.error_link_already_occupied));
                                            dialogSetUsername(s);
                                            return;
                                        }
                                    }
                                    MyToast.toast(mContext, "Error code " + er.code + "\n" + er.message);
                                    dialogSetUsername(s);
                                }
                            }
                        });
                    }
                })
                .show();
        final Button btnPositive = d.getButton(DialogInterface.BUTTON_POSITIVE);

        edtInputUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (s.length() < 5) {
                    btnPositive.setEnabled(false);
                    tvValueHint.setText("Minimum length: 5 characters");
                } else if (str.contains(" ") || str.contains(".") || str.contains("-")) {
                    tvValueHint.setText("Available characters: 'a-z' '0-9' and '_' ");
                    btnPositive.setEnabled(false);
                } else if (Utils.isNumeric(str.substring(0, 1))) {
                    tvValueHint.setText("Link can't starts with numeric");
                    btnPositive.setEnabled(false);
                } else {
                    tvValueHint.setText("");
                    btnPositive.setEnabled(true);
                }
            }
        });
    }

    void confirmConvertToSuperGroup() {
        String msg = getString(R.string.hint_upgrade_to_supergroup);

        new AlertDialog.Builder(mContext)
                .setTitle("Upgrade group")
                .setMessage(msg)
                .setNegativeButton(R.string.btnCancel, null)
                .setPositiveButton(R.string.btnSave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        convertGroupToSuperGroup();
                    }
                })
                .show();
    }

    private void convertGroupToSuperGroup() {
        final ProgressDialog prg = new ProgressDialogBuilder(mContext)
                .setTitle("Loading")
                .setMessage("Please wait...")
                .setIndeterminate(true)
                .setCancelable(false)
                .show();


        new ConvertToSuperGroup(chatId, new Client.ResultHandler() {
            @Override
            public void onResult(final TdApi.TLObject object) {
                onUiThread(new Runnable() {
                    @Override
                    public void run() {
                        prg.dismiss();
                        onConvertGroupToSuperGroupCallback.onResult(object);
                    }
                });
            }
        }).convert();
    }

    Client.ResultHandler onConvertGroupToSuperGroupCallback = new Client.ResultHandler() {
        @Override
        public void onResult(TdApi.TLObject object) {
            btnConvertToSuper.setVisibility(View.GONE);
            TdApi.Chat chat = null;
            if (object.getConstructor() == TdApi.Chat.CONSTRUCTOR)
                chat = (TdApi.Chat) object;
            final boolean success = chat != null && chat.type.getConstructor() == TdApi.ChannelChatInfo.CONSTRUCTOR;
            if (success)
                showConvertSuccessAndFinish();
            else {
                MyToast.toastL(mContext, object.toString());
            }
        }
    };

    private void showConvertSuccessAndFinish() {
        new AlertDialog.Builder(mContext).setCancelable(false)
                .setTitle("Success")
                .setMessage(R.string.action_group_was_converted)
                .setNegativeButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityGroupsList.reloadOnResume = true;
                        finish();
                    }
                })
                .show();
    }

    public static class ConvertToSuperGroup {
        Client.ResultHandler onConverted;
        long chatId;

        public ConvertToSuperGroup(long chatId, Client.ResultHandler onConvertedCallback) {
            this.chatId = chatId;
            this.onConverted = onConvertedCallback;
        }

        void convert() {
            convertGroup();
        }

        void convertGroup() {
            TdApi.TLFunction f = new TdApi.MigrateGroupChatToChannelChat(chatId);
            TgH.send(f, new Client.ResultHandler() {
                @Override
                public void onResult(final TdApi.TLObject object) {
                    onConverted.onResult(object);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (mCurrentPopupWindow != null) {
            mCurrentPopupWindow.dismiss();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void close() {
        AdHelper.onCloseActivity(this);
    }
}
