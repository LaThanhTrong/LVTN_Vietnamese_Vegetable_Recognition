package com.lathanhtrong.lvtn.Fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lathanhtrong.lvtn.Activities.CultivateActivity;
import com.lathanhtrong.lvtn.Activities.ItemDescriptionActivity;
import com.lathanhtrong.lvtn.Adapters.ItemAdapter;
import com.lathanhtrong.lvtn.DBHandler;
import com.lathanhtrong.lvtn.Models.Item;
import com.lathanhtrong.lvtn.Others.TextAccents;
import com.lathanhtrong.lvtn.Others.Utils;
import com.lathanhtrong.lvtn.R;
import com.lathanhtrong.lvtn.databinding.FragmentItemsBinding;
import java.util.ArrayList;
import java.util.List;

public class ItemsFragment extends Fragment implements ItemAdapter.OnItemClickListener {
    private FragmentItemsBinding binding;
    private RecyclerView recyclerView;
    private DBHandler dbHandler;
    private int item_id = -1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentItemsBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();
        if (bundle != null) {
            setItem_id(bundle.getInt("item_id", -1));
        }

        return binding.getRoot();
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHandler = new DBHandler(getContext());

        loadRecyclerView();
        loadAutoComplete();
        //Toast.makeText(getContext(), getItem_id(), Toast.LENGTH_SHORT).show();
        if (getItem_id() != -1) {
            highlightSelectedItem(item_id);
        }

        binding.btnItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ItemDescriptionActivity.class);
                startActivity(intent);
            }
        });

        binding.btnCultivar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getItem_id() == -1) {
                    Toast.makeText(getContext(), getString(R.string.err_pick_item), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), CultivateActivity.class);
                    intent.putExtra("item_id", getItem_id());
                    intent.putExtra("cul_id", 1);
                    startActivity(intent);
                }
            }
        });

        binding.btnLandscape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItem_id() == -1) {
                    Toast.makeText(getContext(), getString(R.string.err_pick_item), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), CultivateActivity.class);
                    intent.putExtra("item_id", getItem_id());
                    intent.putExtra("cul_id", 2);
                    startActivity(intent);
                }
            }
        });

        binding.btnPlantp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItem_id() == -1) {
                    Toast.makeText(getContext(), getString(R.string.err_pick_item), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), CultivateActivity.class);
                    intent.putExtra("item_id", getItem_id());
                    intent.putExtra("cul_id", 3);
                    startActivity(intent);
                }
            }
        });

        binding.btnPlant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItem_id() == -1) {
                    Toast.makeText(getContext(), getString(R.string.err_pick_item), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), CultivateActivity.class);
                    intent.putExtra("item_id", getItem_id());
                    intent.putExtra("cul_id", 4);
                    startActivity(intent);
                }
            }
        });

        binding.btnCare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItem_id() == -1) {
                    Toast.makeText(getContext(), getString(R.string.err_pick_item), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), CultivateActivity.class);
                    intent.putExtra("item_id", getItem_id());
                    intent.putExtra("cul_id", 5);
                    startActivity(intent);
                }
            }
        });

        binding.btnHarvest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItem_id() == -1) {
                    Toast.makeText(getContext(), getString(R.string.err_pick_item), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), CultivateActivity.class);
                    intent.putExtra("item_id", getItem_id());
                    intent.putExtra("cul_id", 6);
                    startActivity(intent);
                }
            }
        });

        binding.btnItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getItem_id() == -1) {
                    Toast.makeText(getContext(), getString(R.string.err_pick_item), Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getActivity(), ItemDescriptionActivity.class);
                    intent.putExtra("item_id", getItem_id());
                    startActivity(intent);
                }
            }
        });

        binding.searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = binding.search.getText().toString();
                if (s.isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.err_search_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                ArrayList<Item> items = new ArrayList<>();
                items = dbHandler.searchItemsbyName(s);
                if (items == null || items.isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.err_search_not_found), Toast.LENGTH_SHORT).show();
                    binding.sivImage.setImageResource(R.drawable.notfound);
                    binding.tvName.setText("");
                    binding.tvSname.setText("");
                    setItem_id(-1);
                    loadRecyclerView();
                }
                else {
                    Item firstItem = items.get(0);
                    highlightSelectedItem(firstItem.getItem_id());
                    loadRecyclerView();
                }
            }
        });

    }

    public void loadAutoComplete() {
        String language = Utils.getLanguage(requireContext());
        ArrayList<Item> items = new ArrayList<>();
        items = dbHandler.getItems();
        ArrayList<Item> displayItems = new ArrayList<>();
        for (Item item : items) {
            if (language.equals("vi")) {
                Item displayItem = new Item(item.getItem_id(), item.getItem_nameVi(), item.getItem_nameVi(), item.getItem_nameVi2(),
                        item.getItem_sname(), item.getItem_descriptionVi(), item.getItem_descriptionVi(),
                        item.getItem_image(), item.getItem_clipart());
                displayItems.add(displayItem);
            } else {
                displayItems.add(item);
            }
        }

        ArrayList<String> itemNames = new ArrayList<>();
        for (Item item : displayItems) {
            itemNames.add(item.getItem_name());
        }

        TextAccents adapter = new TextAccents(getContext(), R.layout.custom_autocomplete, R.id.autoCompleteItem ,itemNames);
        binding.search.setAdapter(adapter);
        binding.search.setThreshold(1);
    }

    public void loadRecyclerView() {
        ArrayList<Item> items = new ArrayList<>();
        String language = Utils.getLanguage(requireContext());
        items = dbHandler.getItems();
        ArrayList<Item> displayItems = new ArrayList<>();
        for (Item item : items) {
            if (language.equals("vi")) {
                Item displayItem = new Item(item.getItem_id(), item.getItem_nameVi(), item.getItem_nameVi(), item.getItem_nameVi2(),
                        item.getItem_sname(), item.getItem_descriptionVi(), item.getItem_descriptionVi(),
                        item.getItem_image(), item.getItem_clipart());
                displayItems.add(displayItem);
            } else {
                displayItems.add(item);
            }
        }

        recyclerView = binding.rvItems;
        ItemAdapter adapter = new ItemAdapter(displayItems, this);
        recyclerView.setAdapter(adapter);

        if (getItem_id() != -1) {
            int selectedPosition = getPositionByItemId(item_id, displayItems);
            if (selectedPosition != -1) {
                recyclerView.scrollToPosition(selectedPosition);
                adapter.setSelectedPosition(selectedPosition);
            }
        }
    }

    private int getPositionByItemId(int itemId, List<Item> items) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItem_id() == itemId) {
                return i;
            }
        }
        return -1;
    }

    private void highlightSelectedItem(int itemId) {
        setItem_id(itemId);
        // Load the item details using the item_id
        String language = Utils.getLanguage(requireContext());
        Item item = dbHandler.getItembyId(itemId);
        Item displayItem = null;
        if (language.equals("vi")) {
            displayItem = new Item(item.getItem_id(), item.getItem_nameVi(), item.getItem_nameVi(), item.getItem_nameVi2(),
                    item.getItem_sname(), item.getItem_descriptionVi(), item.getItem_descriptionVi(),
                    item.getItem_image(), item.getItem_clipart());
        } else {
            displayItem = item;
        }
        if (displayItem != null) {
            binding.tvName.setText(displayItem.getItem_name());
            binding.tvSname.setText(getString(R.string.science_name)+" "+displayItem.getItem_sname());

            String baseImageName = displayItem.getItem_image();
            String[] possibleExtensions = {".png", ".jpg", ".jpeg"};

            for (String extension : possibleExtensions) {
                String imagePath = "file:///android_asset/images/" + baseImageName + extension;

                try {
                    Glide.with(this)
                            .load(imagePath)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(binding.sivImage);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int getItem_id() {
        return item_id;
    }

    public void setItem_id(int item_id) {
        this.item_id = item_id;
    }

    @Override
    public void onItemClick(int itemId) {
        highlightSelectedItem(itemId);
    }
}