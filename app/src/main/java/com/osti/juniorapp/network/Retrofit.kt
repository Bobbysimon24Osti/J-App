package com.osti.juniorapp.network

import com.google.gson.GsonBuilder
import com.osti.juniorapp.db.ParamManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*


class RetrofitClientAttivazione private constructor() {
    private val myApi: ApiOsti
    fun getMyApi(): ApiOsti {
        return myApi
    }

    private val okHttpClient = OkHttpClient.Builder()
        .callTimeout(5000L, TimeUnit.MILLISECONDS)
        .build()

    companion object {
        @get:Synchronized
        var instance: RetrofitClientAttivazione? = null
            get() {
                if (field == null) {
                    field = RetrofitClientAttivazione()
                }
                return field
            }
            private set
    }

    init {
        val gson = GsonBuilder().setLenient().create()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(ApiOsti.BASE_URL_OSTI)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        myApi = retrofit.create<ApiOsti>(ApiOsti::class.java)
    }
}

class RetrofitClientJuniorwe private constructor() {
    object UnsafeOkHttpClient {
        // Create a trust manager that does not validate certificate chains
        val unsafeOkHttpClient: OkHttpClient.Builder

        // Install the all-trusting trust manager

            // Create an ssl socket factory with our all-trusting manager
            get() = try {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        @Throws(CertificateException::class)
                        override fun checkClientTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        @Throws(CertificateException::class)
                        override fun checkServerTrusted(
                            chain: Array<X509Certificate?>?,
                            authType: String?
                        ) {
                        }

                        override fun getAcceptedIssuers(): Array<X509Certificate> {
                            return arrayOf()
                        }
                    }
                )

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())

                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory
                val builder = OkHttpClient.Builder()
                builder.sslSocketFactory(
                    sslSocketFactory,
                    trustAllCerts[0] as X509TrustManager
                )
                builder.protocols(List(2){Protocol.HTTP_2; Protocol.HTTP_1_1})
                builder.hostnameVerifier(HostnameVerifier { hostname, session -> true })
            } catch (e: java.lang.Exception) {
                throw RuntimeException(e)
            }
    }

    private lateinit var myApi: ApiCliente
    fun getMyApi(): ApiCliente? {
        try{
            val httpClient = UnsafeOkHttpClient.unsafeOkHttpClient

            httpClient.addInterceptor(Interceptor { chain ->
                val request: Request =
                    chain.request().newBuilder()
                        .build()
                chain.proceed(request)
            })

            val tmp = ParamManager.getUrl()
            val gson = GsonBuilder().setLenient().create()
            val retrofit: Retrofit = Retrofit.Builder().baseUrl(tmp!!)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build()
            myApi = retrofit.create<ApiCliente>(ApiCliente::class.java)
            return myApi
        }
        catch(e:Exception){
            val i = e
            return null
        }
    }

    companion object {
        @get:Synchronized
        var instance: RetrofitClientJuniorwe? = null
            get() {
                if (field == null) {
                    field = RetrofitClientJuniorwe()
                }
                return field
            }
            private set
    }

}