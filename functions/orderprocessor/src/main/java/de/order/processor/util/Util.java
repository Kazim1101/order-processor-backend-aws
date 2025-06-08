package de.order.processor.util;

import java.util.List;
import java.util.Map;

public class Util {

    public static List<Map<String, Object>> getMenu(){
        return List.of(
                // Appetizers
                Map.of("id", "1", "category", "Appetizers", "name", "Spring Rolls", "description", "Crispy rolls filled with vegetables", "price", "€5.99", "imageUrl", "https://images.unsplash.com/photo-1504674900247-0877df9cc836"),
                Map.of("id", "2", "category", "Appetizers", "name", "Garlic Bread", "description", "Toasted bread with garlic and herbs", "price", "€4.50", "imageUrl", "https://images.unsplash.com/photo-1504674900247-0877df9cc836"),
                Map.of("id", "3", "category", "Appetizers", "name", "Bruschetta", "description", "Grilled bread with tomato and basil", "price", "€6.00", "imageUrl", "https://images.unsplash.com/photo-1504674900247-0877df9cc836"),
                Map.of("id", "4", "category", "Appetizers", "name", "Stuffed Mushrooms", "description", "Mushrooms stuffed with cheese and herbs", "price", "€5.75", "imageUrl", "https://images.unsplash.com/photo-1504674900247-0877df9cc836"),
                Map.of("id", "5", "category", "Appetizers", "name", "Nachos", "description", "Tortilla chips with melted cheese", "price", "€6.50", "imageUrl", "https://images.unsplash.com/photo-1502741338009-cac2772e18bc"),
                Map.of("id", "6", "category", "Appetizers", "name", "Mozzarella Sticks", "description", "Fried mozzarella cheese sticks", "price", "€6.75", "imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"),
                Map.of("id", "7", "category", "Appetizers", "name", "Onion Rings", "description", "Golden fried onion rings", "price", "€4.99", "imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"),
                Map.of("id", "8", "category", "Appetizers", "name", "Chicken Wings", "description", "Spicy buffalo wings", "price", "€7.99", "imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"),
                Map.of("id", "9", "category", "Appetizers", "name", "Hummus & Pita", "description", "Creamy hummus served with warm pita bread", "price", "€5.99", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "10", "category", "Appetizers", "name", "Deviled Eggs", "description", "Classic deviled eggs with a tangy twist", "price", "€4.25", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),

                // Main Course
                Map.of("id", "11", "category", "Main Course", "name", "Grilled Chicken", "description", "Juicy grilled chicken with herbs", "price", "€12.99", "imageUrl", "https://images.unsplash.com/photo-1514512364185-4c2b678c5a16"),
                Map.of("id", "12", "category", "Main Course", "name", "Beef Steak", "description", "Tender grilled steak with sides", "price", "€18.99", "imageUrl", "https://images.unsplash.com/photo-1551183053-bf91a1d81141"),
                Map.of("id", "13", "category", "Main Course", "name", "Salmon Fillet", "description", "Grilled salmon with lemon butter", "price", "€17.50", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "14", "category", "Main Course", "name", "Chicken Alfredo", "description", "Creamy pasta with grilled chicken", "price", "€13.99", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "15", "category", "Main Course", "name", "Vegetable Stir Fry", "description", "Mixed vegetables stir-fried in soy sauce", "price", "€11.50", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "16", "category", "Main Course", "name", "Shrimp Tacos", "description", "Soft tacos filled with grilled shrimp", "price", "€14.25", "imageUrl", "https://images.unsplash.com/photo-1502741338009-cac2772e18bc"),
                Map.of("id", "17", "category", "Main Course", "name", "Lamb Chops", "description", "Grilled lamb chops with herbs", "price", "€19.00", "imageUrl", "https://images.unsplash.com/photo-1551183053-bf91a1d81141"),
                Map.of("id", "18", "category", "Main Course", "name", "Turkey Meatballs", "description", "Healthy turkey meatballs in marinara", "price", "€12.00", "imageUrl", "https://images.unsplash.com/photo-1546069901-ba9599a7e63c"),
                Map.of("id", "19", "category", "Main Course", "name", "Fish & Chips", "description", "Classic battered fish with fries", "price", "€13.75", "imageUrl", "https://images.unsplash.com/photo-1504674900247-0877df9cc836"),
                Map.of("id", "20", "category", "Main Course", "name", "Stuffed Peppers", "description", "Bell peppers stuffed with beef and rice", "price", "€12.50", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),

                // Pizza
                Map.of("id", "21", "category", "Pizza", "name", "Margherita", "description", "Classic tomato, mozzarella, and basil", "price", "€10.99", "imageUrl", "https://images.unsplash.com/photo-1548365328-8b849e6c7c7e"),
                Map.of("id", "22", "category", "Pizza", "name", "Pepperoni", "description", "Mozzarella and pepperoni slices", "price", "€11.99", "imageUrl", "https://images.unsplash.com/photo-1548365328-8b849e6c7c7e"),
                Map.of("id", "23", "category", "Pizza", "name", "BBQ Chicken", "description", "Grilled chicken and BBQ sauce", "price", "€12.99", "imageUrl", "https://images.unsplash.com/photo-1548365328-8b849e6c7c7e"),
                Map.of("id", "24", "category", "Pizza", "name", "Veggie Delight", "description", "Mixed vegetables and cheese", "price", "€11.50", "imageUrl", "https://images.unsplash.com/photo-1548365328-8b849e6c7c7e"),
                Map.of("id", "25", "category", "Pizza", "name", "Hawaiian", "description", "Ham and pineapple combo", "price", "€12.25", "imageUrl", "https://images.unsplash.com/photo-1548365328-8b849e6c7c7e"),

                // Pasta
                Map.of("id", "26", "category", "Pasta", "name", "Spaghetti Bolognese", "description", "Traditional meat sauce pasta", "price", "€11.99", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "27", "category", "Pasta", "name", "Penne Arrabbiata", "description", "Spicy tomato sauce pasta", "price", "€10.99", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "28", "category", "Pasta", "name", "Lasagna", "description", "Layers of pasta, cheese and meat", "price", "€12.99", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "29", "category", "Pasta", "name", "Mac & Cheese", "description", "Cheesy macaroni pasta", "price", "€9.99", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "30", "category", "Pasta", "name", "Fettuccine Alfredo", "description", "Creamy Alfredo pasta", "price", "€11.50", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),

                // Hot Drinks
                Map.of("id", "31", "category", "Hot Drinks", "name", "Espresso", "description", "Strong and rich espresso shot", "price", "€2.99", "imageUrl", "https://images.unsplash.com/photo-1432888498266-38ffec3eaf0a"),
                Map.of("id", "32", "category", "Hot Drinks", "name", "Cappuccino", "description", "Espresso with steamed milk and foam", "price", "€3.50", "imageUrl", "https://images.unsplash.com/photo-1432888498266-38ffec3eaf0a"),
                Map.of("id", "33", "category", "Hot Drinks", "name", "Latte", "description", "Creamy milk-based coffee", "price", "€3.75", "imageUrl", "https://images.unsplash.com/photo-1432888498266-38ffec3eaf0a"),
                Map.of("id", "34", "category", "Hot Drinks", "name", "Hot Chocolate", "description", "Rich chocolate drink", "price", "€3.25", "imageUrl", "https://images.unsplash.com/photo-1432888498266-38ffec3eaf0a"),
                Map.of("id", "35", "category", "Hot Drinks", "name", "Black Tea", "description", "Classic black tea", "price", "€2.50", "imageUrl", "https://images.unsplash.com/photo-1432888498266-38ffec3eaf0a"),

                // Warm Drinks
                Map.of("id", "36", "category", "Warm Drinks", "name", "Green Tea", "description", "Light and refreshing", "price", "€2.75", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "37", "category", "Warm Drinks", "name", "Herbal Tea", "description", "Caffeine-free herbal infusion", "price", "€2.95", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "38", "category", "Warm Drinks", "name", "Chai Latte", "description", "Spiced milk tea", "price", "€3.25", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "39", "category", "Warm Drinks", "name", "Matcha", "description", "Japanese powdered green tea", "price", "€3.99", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288"),
                Map.of("id", "40", "category", "Warm Drinks", "name", "Warm Apple Cider", "description", "Spiced hot apple cider", "price", "€3.50", "imageUrl", "https://images.unsplash.com/photo-1467003909585-2f8a72700288")
        );
    }
}
