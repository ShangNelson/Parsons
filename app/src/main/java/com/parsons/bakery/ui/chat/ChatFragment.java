package com.parsons.bakery.ui.chat;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parsons.bakery.DBHandler;
import com.parsons.bakery.R;
import com.parsons.bakery.databinding.FragmentChatBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ChatFragment extends Fragment  {

    private FragmentChatBinding binding;
    public static final Object ob = new Object();
    public View root;

    public String TAG = "ChatFrag";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentChatBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        RecyclerView view = root.findViewById(R.id.listOfPeople);

        DBHandler dbHandler = new DBHandler(getContext());
        List<HashMap<String, String>> results = dbHandler.executeOne("SELECT * FROM people");
        List<Name> names = new ArrayList<>();
        for (HashMap<String, String> person : results) {
            names.add(new Name(person.get("name"), person.get("unique_id")));
        }
        LinearLayoutManager ln = new LinearLayoutManager(getContext());
        view.setLayoutManager(ln);
        view.setAdapter(new RecyclerAdapter(getContext(), names));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, new int[]{0xfff7f7f7, 0xfff7f7f7});
        drawable.setSize(1, 30);
        itemDecoration.setDrawable(drawable);
        view.addItemDecoration(itemDecoration);



        FloatingActionButton add = root.findViewById(R.id.addPerson);
        add.setOnClickListener(view1 -> {
            DialogFragment dialogFragment = new AddDialog(getContext());
            dialogFragment.show(getChildFragmentManager(), "Add");
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}