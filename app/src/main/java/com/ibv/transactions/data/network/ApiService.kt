package com.ibv.transactions.data.network



import com.ibv.transactions.dashBoard.fragment.model.TransactionBean
import com.ibv.transactions.others.Cons
import com.ibv.transactions.userAuth.loginBean.LoginRequest
import com.ibv.transactions.userAuth.loginBean.LoginBean
import retrofit2.Response
import retrofit2.http.*

/**
 * Changes has been done on 21 March 2024 by MrDev(Mahesh)
 *

 * */


interface ApiService {

    @POST(Cons.LOGIN)
    //suspend fun loginWithEmail(@Body request: LoginRequest): LoginBean
    suspend fun login(@Body request: LoginRequest): Response<LoginBean> // return type to Response<LoginBean>


    @POST(Cons.TRANSACTION)
    suspend fun getTransactionsList(): Response<List<TransactionBean>> // return Response

}








