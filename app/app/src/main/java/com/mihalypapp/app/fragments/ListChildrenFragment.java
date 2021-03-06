package com.mihalypapp.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mihalypapp.app.R;
import com.mihalypapp.app.activities.AddChildActivity;
import com.mihalypapp.app.activities.ChildActivity;
import com.mihalypapp.app.activities.MainActivity;
import com.mihalypapp.app.adapters.ChildCardAdapter;
import com.mihalypapp.app.models.Child;
import com.mihalypapp.app.models.EndlessRecyclerViewScrollListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListChildrenFragment extends Fragment {

    private static final String TAG = "ListChildrenFragment";

    private static final int RC_ADD_CHILD = 10;

    private ArrayList<Child> childList = new ArrayList<>();

    private boolean refreshing = false;
    private boolean fetching = false;
    private boolean showingProgressBar = false;
    private int offset = 0;
    private String filter = "";

    private RecyclerView recyclerView;
    private ChildCardAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_list_children, container, false);

        setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(getString(R.string.children));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        adapter = new ChildCardAdapter(childList);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (!fetching) {
                    addProgressBar();
                    fetchChildren();
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);

        swipeContainer = view.findViewById(R.id.swipe_container);
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!fetching) {
                    refreshing = true;
                    offset = 0;
                    fetchChildren();
                }
            }
        });

        adapter.setOnItemClickListener(new ChildCardAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                int childId = childList.get(position).getId();
                Intent intent = new Intent(getContext(), ChildActivity.class);
                intent.putExtra(ChildActivity.CHILD_ID, childId);
                startActivity(intent);
            }
        });

        FloatingActionButton floatingActionButton = view.findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddChildActivity.class);
                startActivityForResult(intent, RC_ADD_CHILD);
            }
        });

        fetchChildren();
        return view;
    }

    private void fetchChildren() {
        fetching = true;

        final JSONObject params = new JSONObject();
        try {
            params.put("offset", offset);
            params.put("quantity", 15);
            params.put("filter", filter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObjectRequest getChildrenRequest = new JsonObjectRequest(Request.Method.POST, MainActivity.URL + "children", params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getString("status").equals("success")) {
                                Log.i(TAG, response.toString());
                                JSONArray children = response.getJSONArray("children");

                                removeProgressBar();

                                if (refreshing) {
                                    childList.clear();
                                    adapter.notifyDataSetChanged();
                                }

                                int i;
                                for (i = 0; i < children.length(); i++) {
                                    JSONObject child = children.getJSONObject(i);
                                    childList.add(new Child(
                                            child.getInt("childId"),
                                            R.drawable.ic_launcher_foreground,
                                            child.getString("childName"),
                                            child.getString("groupType"),
                                            child.getString("parentName"),
                                            child.getString("parentEmail")
                                    ));
                                    offset++;
                                }

                                if (!refreshing) {
                                    adapter.notifyItemRangeInserted(childList.size() - i, i);
                                } else {
                                    adapter.notifyDataSetChanged();
                                    scrollListener.resetState();
                                    refreshing = false;
                                }

                            } else {
                                Log.e(TAG, "getChildrenRequest ERROR");
                            }

                            swipeContainer.setRefreshing(false);
                            fetching = false;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, error.toString());
            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(getChildrenRequest);
    }

    private void addProgressBar() {
        showingProgressBar = true;
        childList.add(null);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                adapter.notifyItemInserted(childList.size() - 1);
            }
        });
    }

    private void removeProgressBar() {
        if (showingProgressBar) {
            showingProgressBar = false;
            childList.remove(childList.size() - 1);
            adapter.notifyItemRemoved(childList.size());
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (!fetching) {
                    refreshing = true;
                    offset = 0;
                    filter = query;
                    fetchChildren();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!fetching) {
                    refreshing = true;
                    offset = 0;
                    filter = newText;
                    fetchChildren();
                }
                return false;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_ADD_CHILD) {
            if (resultCode == Activity.RESULT_OK) {
                refreshing = true;
                offset = 0;
                fetchChildren();
            }
        }
    }
}
