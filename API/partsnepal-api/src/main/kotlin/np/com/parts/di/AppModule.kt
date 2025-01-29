package np.com.parts.di


import org.koin.dsl.module
import np.com.parts.system.Services.*

val appModule = module {
//    single { connect() }
    single { ProductService(get()) }
    single { UserService(get()) }
    single { CartService(get(), get()) }
    single { OrderService(get(), get(), get()) }
    single { PasteService(get()) }
    single { PaymentService(get(), get(), get()) }
    single { CategoryService(get()) }
}