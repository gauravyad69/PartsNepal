package np.com.parts.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import np.com.parts.API.Repository.AuthRepository
import np.com.parts.API.Repository.KhaltiPaymentRepository
import np.com.parts.API.Repository.OrderRepository
import np.com.parts.API.Repository.ProductRepository
import np.com.parts.API.Repository.UserRepository
import np.com.parts.API.TokenManager
import np.com.parts.database.AppDatabase
import np.com.parts.repository.CartRepository
import np.com.parts.app_utils.SyncManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
   @Singleton
   fun provideAuthRepository(
       client: HttpClient,
       tokenManager: TokenManager
   ): AuthRepository {
       return AuthRepository(client, tokenManager)
   }
    @Provides
   @Singleton
   fun provideUserRepository(client: HttpClient): UserRepository {
       return UserRepository(client)
   }
    @Provides
   @Singleton
   fun provideProductRepository(client: HttpClient): ProductRepository {
       return ProductRepository(client)
   }
    @Provides
   @Singleton
   fun provideKhaltiPaymentRepository(
       client: HttpClient
   ): KhaltiPaymentRepository {
       return KhaltiPaymentRepository(client)
   }
    @Provides
    @Singleton
    fun provideOrderRepository(httpClient: HttpClient): OrderRepository {
        return OrderRepository(httpClient)
    }

    @Provides
    @Singleton
    fun provideCartRepository(database: AppDatabase, client: HttpClient): CartRepository {
        return CartRepository(database, client)
    }


    @Provides
    @Singleton
    fun provideSyncManager(cartRepository: CartRepository): SyncManager {
        return SyncManager(cartRepository)
    }
} 