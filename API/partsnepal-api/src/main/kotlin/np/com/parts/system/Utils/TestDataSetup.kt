package np.com.parts.system.Utils

import kotlinx.coroutines.runBlocking
import np.com.parts.system.Models.*
import np.com.parts.system.Services.*
import org.bson.types.ObjectId
import java.util.*

class TestDataSetup(
    private val orderService: OrderService,
    private val productService: ProductService,
    private val userService: UserService,
    private val cartService: CartService
) {
    fun setupAll() = runBlocking {
        setupProducts()
        setupUsers()
        setupCarts()
        setupOrders()
        println("Test data setup completed successfully!")
    }

    private suspend fun setupProducts() {
        val products = listOf(
            createProduct(
                id = 123,
                name = "Brake Pad Set",
                price = 2500,
                stock = 50
            ),
            createProduct(
                id = 124,
                name = "Oil Filter",
                price = 1200,
                stock = 100
            ),
            createProduct(
                id = 125,
                name = "Air Filter",
                price = 1500,
                stock = 75
            )
        )

        products.forEach { product ->
            productService.createProduct(product)
        }
    }

    private suspend fun setupUsers() {
        val users = listOf(
            createFullUserDetails(
                id = 1,
                username = "johndoe",
                email = "john@example.com",
                phone = "+9779812345678"
            ),
            createFullUserDetails(
                id = 2,
                username = "janesmith",
                email = "jane@example.com",
                phone = "+9779823456789"
            )
        )

        users.forEach { user ->
            userService.createUser(user)
        }
    }

    private suspend fun setupCarts() {
        // Add items to John's cart
        cartService.addToCart(1, 123, 2)
        cartService.addToCart(1, 124, 1)

        // Add items to Jane's cart
        cartService.addToCart(2, 125, 1)
    }

    private suspend fun setupOrders() {
        val orders = listOf(
            createOrder(1, "PENDING_PAYMENT"),
            createOrder(2, "PROCESSING")
        )

        orders.forEach { order ->
            orderService.createOrder(order.customer.id, CreateOrderRequest(
                items = order.items,
                paymentMethod = order.payment.method,
                shippingDetails = order.shippingDetails
            ))
        }
    }

    private fun createProduct(
        id: Int,
        name: String,
        price: Long,
        stock: Int
    ) = ProductModel(
        id = ObjectId().toString(),
        basic = BasicProductInfo(
            productId = id,
            productSKU = "SKU-$id",
            productName = name,
            productType = "Auto Parts",
            inventory = InventoryInfo(
                stock = stock,
                mainImage = "https://example.com/images/$id.jpg",
                isAvailable = true
            ),
            pricing = PricingInfo(
                regularPrice = Money(price),
                salePrice = null,
                discount = null
            )
        ),
        details = DetailedProductInfo(
            productId = id,
            description = "High-quality $name",
            features = Features(
                highlights = listOf("Durable", "High performance")
            ),
            delivery = DeliveryInfo(),
            warranty = WarrantyInfo(
                isReturnable = true,
                warrantyMonths = 12
            )
        )
    )

    private fun createFullUserDetails(
        id: Int,
        username: String,
        email: String,
        phone: String
    ): FullUserDetails {
        val firstName = username.substring(0, 4).capitalize()
        val lastName = username.substring(4).capitalize()
        
        return FullUserDetails(
            user = UserModel(
                userId = UserId(id),
                username = username,
                email = Email(email),
                firstName = firstName,
                lastName = lastName,
                phoneNumber = PhoneNumber(phone),
                accountType = AccountType.PERSONAL,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            credentials = UserCredentials(
                hashedPassword = null, // Set this if needed
                lastPasswordChange = System.currentTimeMillis(),
                mfaEnabled = false
            ),
            preferences = UserPreferences(
                language = "en",
                timezone = "Asia/Kathmandu",
                marketingConsent = true,
                notificationSettings = NotificationSettings(
                    emailNotifications = true,
                    pushNotifications = true,
                    smsNotifications = true
                )
            ),
            engagement = UserEngagement(
                totalTimeSpentMs = 0,
                lastActive = System.currentTimeMillis(),
                engagementScore = 0,
                loginHistory = emptyList()
            ),
            accountStatus = AccountStatus.ACTIVE,
            reviews = UserReviews(),
            orders = UserOrders(),
            lastModifiedAt = System.currentTimeMillis()
        )
    }

    private fun createOrder(
        userId: Int,
        status: String
    ) = OrderModel(
        id = ObjectId().toString(),
        orderNumber = "ORD-${System.currentTimeMillis()}-${Random().nextInt(9999)}",
        items = listOf(
            LineItem(
                id = ObjectId().toString(),
                productId = 123,
                name = "Brake Pad Set",
                quantity = 2,
                unitPrice = Money(2500)
            )
        ),
        customer = CustomerInfo(
            id = userId,
            name = "User $userId",
            type = CustomerType.INDIVIDUAL
        ),
        payment = PaymentInfo(
            method = PaymentMethod.CASH_ON_DELIVERY,
            status = PaymentStatus.PENDING
        ),
        shippingDetails = ShippingDetails(
            address = ShippingAddress(
                street = "Street $userId",
                city = "Kathmandu",
                province = "Bagmati",
                district = "Kathmandu",
                ward = 1,
                landmark = "Near Landmark $userId",
                recipient = RecipientInfo(
                    name = "User $userId",
                    phone = "+977981234567$userId"
                )
            ),
            method = ShippingMethod.STANDARD,
            cost = Money(100)
        ),
        status = OrderStatus.valueOf(status),
        source = OrderSource.MOBILE_APP,
        summary = OrderSummary(
           subtotal = Money(100),
            total = Money(100)
        )
    )
} 