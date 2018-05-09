package com.sdex.activityrunner.shortcut

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputLayout
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.sdex.activityrunner.R
import com.sdex.activityrunner.db.activity.ActivityModel
import com.sdex.activityrunner.db.history.HistoryModel
import com.sdex.activityrunner.glide.GlideApp
import com.sdex.activityrunner.intent.converter.HistoryToLaunchParamsConverter
import com.sdex.activityrunner.intent.converter.LaunchParamsToIntentConverter
import com.sdex.activityrunner.util.IntentUtils

class AddShortcutDialogFragment : DialogFragment() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val activityModel = arguments?.getSerializable(ARG_ACTIVITY_MODEL) as ActivityModel?
    val historyModel = arguments?.getSerializable(ARG_HISTORY_MODEL) as HistoryModel?

    val builder = AlertDialog.Builder(activity!!)
    val view = View.inflate(activity, R.layout.dialog_add_shortcut, null)
    val labelViewLayout = view.findViewById<TextInputLayout>(R.id.value_layout)
    val textName = view.findViewById<EditText>(R.id.shortcut_name)

    textName.setText(activityModel?.name)
    textName.setSelection(textName.text.length)

    val imageIcon = view.findViewById<ImageView>(R.id.app_icon)
    GlideApp.with(this)
      .load(activityModel)
      .error(R.mipmap.ic_launcher)
      .apply(RequestOptions()
        .fitCenter())
      .into(imageIcon)
    builder.setTitle(R.string.context_action_edit)
      .setView(view)
      .setPositiveButton(R.string.context_action_shortcut, null)
      .setNegativeButton(android.R.string.cancel, null)
    val alertDialog = builder.create()
    alertDialog.setOnShowListener { _ ->
      alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
        .setOnClickListener { _ ->
          labelViewLayout.error = null
          val shortcutName = textName.text.toString()
          if (TextUtils.isEmpty(shortcutName)) {
            labelViewLayout.error = getString(R.string.shortcut_name_empty)
            return@setOnClickListener
          }
          if (activityModel != null) {
            activityModel.name = shortcutName
            IntentUtils.createLauncherIcon(activity, activityModel)
          } else {
            createHistoryModelShortcut(historyModel!!, shortcutName)
          }
          dismiss()
        }
    }
    return alertDialog
  }

  private fun createHistoryModelShortcut(historyModel: HistoryModel, shortcutName: String) {
    val historyToLaunchParamsConverter = HistoryToLaunchParamsConverter(historyModel)
    val launchParams = historyToLaunchParamsConverter.convert()
    val converter = LaunchParamsToIntentConverter(launchParams)
    val intent = converter.convert()
    IntentUtils.createLauncherIcon(activity, shortcutName, intent, R.mipmap.ic_launcher)
  }

  companion object {

    const val TAG = "AddShortcutDialogFragment"

    const val ARG_ACTIVITY_MODEL = "arg_activity_model"
    const val ARG_HISTORY_MODEL = "arg_history_model"

    fun newInstance(activityModel: ActivityModel): AddShortcutDialogFragment {
      val args = Bundle(1)
      args.putSerializable(AddShortcutDialogFragment.ARG_ACTIVITY_MODEL, activityModel)
      val fragment = AddShortcutDialogFragment()
      fragment.arguments = args
      return fragment
    }

    fun newInstance(historyModel: HistoryModel): AddShortcutDialogFragment {
      val args = Bundle(1)
      args.putSerializable(AddShortcutDialogFragment.ARG_HISTORY_MODEL, historyModel)
      val fragment = AddShortcutDialogFragment()
      fragment.arguments = args
      return fragment
    }
  }
}
