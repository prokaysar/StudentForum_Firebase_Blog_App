package bd.gov.fci.studentforum;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    private RecyclerView blog_list_view;
    private List<BlogPost> blog_list;

    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisiable;
    Context context;

    private Boolean isFirstPageFirstLoad = true;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_home, container, false);

        blog_list = new ArrayList<>();
        blog_list_view = view.findViewById(R.id.blog_list_view);
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        mAuth = FirebaseAuth.getInstance();

        blog_list_view.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_list_view.setAdapter(blogRecyclerAdapter);
        if (mAuth.getCurrentUser() != null) {
            firestore = FirebaseFirestore.getInstance();
//scroll process
            blog_list_view.setOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    boolean reachBottom = !recyclerView.canScrollVertically(-1);
                    if (reachBottom){
                       loadMore();
                    }
                }
            });

            Query fistQuery = firestore.collection("Post").orderBy("timestamp",Query.Direction.DESCENDING).limit(3);
            fistQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (isFirstPageFirstLoad){
                        lastVisiable = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);
                    }


                    for (DocumentChange docs : documentSnapshots.getDocumentChanges()) {

                        if (docs.getType() == DocumentChange.Type.ADDED) {

                            String id = docs.getDocument().getId();
                            BlogPost blogPost = docs.getDocument().toObject(BlogPost.class).withId(id);

                            if (isFirstPageFirstLoad) {
                                blog_list.add(blogPost);
                            }else {
                                blog_list.add(0,blogPost);
                            }
                            blogRecyclerAdapter.notifyDataSetChanged();

                        }
                    }
                    isFirstPageFirstLoad = false;
                }
            });

        }
        // Inflate the layout for this fragment
        return view;
    }
//create pagination
    private void loadMore(){
        Query nexQuery = firestore.collection("Post")
                .orderBy("timestamp",Query.Direction.DESCENDING)
                .startAfter(lastVisiable)
                .limit(3);
        nexQuery.addSnapshotListener(getActivity(),new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                   lastVisiable = documentSnapshots.getDocuments().get(documentSnapshots.size()-1);

                if (!documentSnapshots.isEmpty()){

                    for (DocumentChange docs : documentSnapshots.getDocumentChanges()) {

                        if (docs.getType() == DocumentChange.Type.ADDED) {

                            String id = docs.getDocument().getId();
                            BlogPost blogPost = docs.getDocument().toObject(BlogPost.class).withId(id);
                            blog_list.add(blogPost);
                            blogRecyclerAdapter.notifyDataSetChanged();

                        }
                    }
                }
            }
        });

    }

}
