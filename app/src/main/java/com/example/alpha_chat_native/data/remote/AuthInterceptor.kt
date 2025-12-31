package com.example.alpha_chat_native.data.remote

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp Interceptor that adds session cookie to all API requests.
 * The cookie is stored by TokenManager after OAuth callback.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val requestBuilder = originalRequest.newBuilder()
        
        // Add session cookie if available
        tokenManager.getSessionCookie()?.let { cookie ->
            requestBuilder.addHeader("Cookie", cookie)
        }
        
        // Add common headers
        requestBuilder.addHeader("Accept", "application/json")
        requestBuilder.addHeader("Content-Type", "application/json")
        
        return chain.proceed(requestBuilder.build())
    }
}
