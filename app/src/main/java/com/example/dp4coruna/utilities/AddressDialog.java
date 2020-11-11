package com.example.dp4coruna.utilities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.dp4coruna.R;

public class AddressDialog extends DialogFragment {

    DialogCallBack callbackListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder ab = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        ab.setView(inflater.inflate(R.layout.dialog_twotextboxes,null))
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callbackListener.onRightButtonPress(AddressDialog.this);
                    }
                })
                .setNegativeButton(R.string.cancelTag, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        callbackListener.onLeftButtonPress(AddressDialog.this);
                    }
                });

        return ab.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try{
            callbackListener = (DialogCallBack) context;
        } catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString()
                    + "Dialog's parent class has to implement DialogCallback");
        }
    }
}
