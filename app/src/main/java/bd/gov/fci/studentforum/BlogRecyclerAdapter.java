package bd.gov.fci.studentforum;


import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {
    public Context context;
    public List<BlogPost> blog_list;

    private  FirebaseFirestore firestore;
    private FirebaseAuth mAuth;

    public BlogRecyclerAdapter(List<BlogPost> blog_list){
        this.blog_list = blog_list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context = parent.getContext();
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);

        final String blogPostId  = blog_list.get(position).BlogPostId;
        final String currentUserId  =mAuth.getCurrentUser().getUid();

        String decs_text = blog_list.get(position).getDesc();
        holder.setDesText(decs_text);

        String image_url = blog_list.get(position).getImage_url();
        String thumb_url = blog_list.get(position).getThumb_image();
        holder.setBlogImage(image_url,thumb_url);

        String user_id = blog_list.get(position).getUser_id();
        // retrieve user info here
        firestore.collection("User").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()){
                    String userName = task.getResult().getString("name");
                    String profileImage = task.getResult().getString("image");
                    holder.setUserDate(userName,profileImage);
                }
            }
        });

        long millisecond = blog_list.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("dd/MM/yyy", new Date(millisecond)).toString();
        holder.setTime(dateString);


        //Count User Likes

        firestore.collection("Post/"+blogPostId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()){
                    int count = documentSnapshots.size();
                    holder.updateLikeCount(count);
                }else {
                    holder.updateLikeCount(0);
                }
            }
        });


        //Get Likes
        firestore.collection("Post/"+blogPostId+"/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (documentSnapshot.exists()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.blogImageButton.setImageDrawable(context.getDrawable(R.mipmap.like_accent));
                    }
                }else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.blogImageButton.setImageDrawable(context.getDrawable(R.mipmap.like_gray));
                    }

                }
            }
        });

        //Likes Feature
        holder.blogImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firestore.collection("Post/"+blogPostId+"/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                        if (!task.getResult().exists()){
                            Map<String,Object> likesMap = new HashMap<>();
                            likesMap.put("timestamp", FieldValue.serverTimestamp());

                            firestore.collection("Post/"+blogPostId+"/Likes").document(currentUserId).set(likesMap);
                        }else {
                            firestore.collection("Post/"+blogPostId+"/Likes").document(currentUserId).delete();

                        }
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView desText;
        private ImageView postImage;
        private TextView dateTxt;
        private TextView blog_userName;
        private CircleImageView blog_profileImage;

        private ImageView blogImageButton;
        private TextView blogLIkesCount;



        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            blogImageButton = mView.findViewById(R.id.blog_like_btn);
        }

        private void setDesText(String text){
            desText = mView.findViewById(R.id.blog_desc);
            desText.setText(text);
        }

        private void setBlogImage(String download_uri,String tumb_image) {
            postImage = mView.findViewById(R.id.blog_image);
            Glide.with(context).load(download_uri).thumbnail(Glide.with(context).load(tumb_image)).into(postImage);

        }
        private void setTime(String time) {
            dateTxt = mView.findViewById(R.id.blog_date);
            dateTxt.setText(time);
        }
        private void setUserDate(String name ,String image){
            blog_userName = mView.findViewById(R.id.blog_user_name);
            blog_profileImage = mView.findViewById(R.id.blog_user_image);

            blog_userName.setText(name);
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.mipmap.account);
            Glide.with(context).setDefaultRequestOptions(requestOptions).load(image).into(blog_profileImage);
        }


        private void updateLikeCount(int count){
            blogLIkesCount = mView.findViewById(R.id.blog_like);
            blogLIkesCount.setText(count+" " +
                    "Likes");
        }
    }

}