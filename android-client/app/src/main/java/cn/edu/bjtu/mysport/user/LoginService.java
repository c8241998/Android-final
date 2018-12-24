package cn.edu.bjtu.mysport.user;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface LoginService {

    @FormUrlEncoded
    @POST("login")
    Call<ResponseBody> login(@Field("email") String email, @Field("password") String password);

    @FormUrlEncoded
    @POST("register")
    Call<ResponseBody> register(@Field("email") String email, @Field("password") String password,@Field("username") String username);

    @FormUrlEncoded
    @POST("addword")
    Call<ResponseBody> addword(@Field("email") String email, @Field("word") String word);

    @GET("getUsername")
    Call<ResponseBody> getUsername(@Query("email") String email);

    @GET("getbook")
    Call<ResponseBody> getBook(@Query("email") String email);



//
}
