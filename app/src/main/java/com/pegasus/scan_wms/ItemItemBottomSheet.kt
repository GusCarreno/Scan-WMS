package com.pegasus.scan_wms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_item_item_bottom_sheet.view.*

class ItemItemBottomSheet(private val context: MainActivity) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_item_item_bottom_sheet, container, true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.txtTakePhoto.setOnClickListener {
            MainActivity().checkPermissions(context, 1)
            dismiss()
        }

    }
}