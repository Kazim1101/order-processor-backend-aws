package de.order.processor.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Util {
    private static final String BASE_URL = "https://order-processor-backend-a-placeorderkzmberlinresta-osn3cwiiang7.s3.eu-central-1.amazonaws.com/menu_images/";

    public static List<Map<String, Object>> getMenu(){
    List<Map<String, ?>> menuItems = List.of(
            // FRÜHSTÜCK / BREAKFAST
            Map.of(
                    "id", "1",
                    "category", "FRÜHSTÜCK / BREAKFAST",
                    "name", "130. Klassische Frühstück",
                    "description", "Brötchen x2, Wurst, Käse, Butter, Konfitüre, gekochtes Ei Continental mit einen Orangensaft 0,1l und Kaffee nach Wahl. Bread Rolls x2, Sausage, Cheese, Butter, Jam, Boiled Egg Continental with a Orange Juice 0,1l and a Coffee of your choice",
                    "price", "€16.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "2",
                    "category", "FRÜHSTÜCK / BREAKFAST",
                    "name", "131. Französisches Frühstück",
                    "description", "Brötchen, Croissant, Butter, Konfitüre und Honig, und ein Kaffee nach Wahl. Bread Roll, Croissant, Butter, Jam and Honey and a Coffee of your choice",
                    "price", "€16.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "3",
                    "category", "FRÜHSTÜCK / BREAKFAST",
                    "name", "132. Italienische Omelette",
                    "description", "Omelette mit Chilliflocken Kirschtomaten, bresaola Basilikum und Kaffe nach Wahl. Omelet with Chili Flakes, Cherry Tomatoes, Smoked Beef and Basil and Coffee of your choice",
                    "price", "€18.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "4",
                    "category", "FRÜHSTÜCK / BREAKFAST",
                    "name", "133. Spanische Omelette",
                    "description", "Omelette mit Kartoffeln, Zwiebeln, Paprika, Kirschtomaten und Kaffee nach Wahl. Omelet with Potatos, Onions, Peper, Cherry Tomatoes and a Coffee of your choice",
                    "price", "€18.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "5",
                    "category", "FRÜHSTÜCK / BREAKFAST",
                    "name", "136. Frischer Orangensaft",
                    "description", "Frischer Orangensaft. Fresh Orange Juice",
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // VORSPEISEN / STARTERS
            Map.of(
                    "id", "6",
                    "category", "VORSPEISEN / STARTERS",
                    "name", "20. Mozzarella Pomodori e Basilico",
                    "description", "Buffelmozzarella mit Tomaten und Basilikum. Buffalo Mozzarella with Tomatoes and Basil",
                    "price", "€15.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "7",
                    "category", "VORSPEISEN / STARTERS",
                    "name", "26. Carpaccio di Manzo",
                    "description", "Vom Rind mit Rucola und gehobeltem Parmesan und dazu Spezialsauce. Beef with Rucola, Parmesan and Special Sauce",
                    "price", "€17.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "8",
                    "category", "VORSPEISEN / STARTERS",
                    "name", "84. Bruschetta",
                    "description", "Brot mit Tomaten, Zwibeln, Knoblauch in Olivenöl. Bread with Tomatoes, Onions, Garlic and Olive-Oil",
                    "price", "€9.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // SALAT / SALAD
            Map.of(
                    "id", "9",
                    "category", "SALAT / SALAD",
                    "name", "30. Insalata Rucola",
                    "description", "Rucolasalat mit Cherrytomaten und frisch geriebenem Parmesan. Rocket Salad with Cherry Tomatoes and freshly grated Parmesan",
                    "price", "€16.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "10",
                    "category", "SALAT / SALAD",
                    "name", "31. Garten Salat",
                    "description", "Champignons, Paprika, Walnuss, Zwiebeln, Rucola und Radieschen mit hausgemachtem Dressing. Mushrooms, Peppers, Walnuts, Onions, Rocket Salad and Radishes with homemade dressing",
                    "price", "€14.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // PASTA
            Map.of(
                    "id", "11",
                    "category", "PASTA",
                    "name", "44. Tagliatelle Avanti",
                    "description", "Tagliatelle mit Filetspitzen, frischen Champignons in zarter Sahnesauce. Tagliatelle with Filet-Stripes, Mushrooms in a delicate Cream Sauce",
                    "price", "€17.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "12",
                    "category", "PASTA",
                    "name", "45. Penne Arrabiata",
                    "description", "Rohrnudeln mit Oliven und Knoblauch in zarter Tomatensauce (Scharf). Noodles with Olive, Garlic and Tomato sauce (hot)",
                    "price", "€15.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "13",
                    "category", "PASTA",
                    "name", "52. Tagliatelle Verde",
                    "description", "Bandnudeln mit getrockneten Tomaten, Rucola, Knoblauch und geriebenem Schafskase. Ribbon-Noodles with Tomatoes, Rucola, Garlic and Sheep's Cheese",
                    "price", "€15.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "14",
                    "category", "PASTA",
                    "name", "53. Tagliatelle Salmone",
                    "description", "Bandnudeln mit Lachs, Zucchini, rosa Sahnesauce und Hummersauce. Ribbon-Noodles with Salmon, Zucchini, Pink-Cream and Lobster sauce",
                    "price", "€18.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // PIZZA
            Map.of(
                    "id", "15",
                    "category", "PIZZA",
                    "name", "71. Verde",
                    "description", "Mit Rucola und geriebenem Parmesan Käse. With Rucola and Parmesan Cheese",
                    "price", "€15.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "16",
                    "category", "PIZZA",
                    "name", "74. Verdure",
                    "description", "Mit verschiedenen Gemüse, Mozzarella, Tomaten und Basilikum. With various Vegetables, Mozzarella, Tomatoes and Basil",
                    "price", "€20.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "17",
                    "category", "PIZZA",
                    "name", "76. Capriciossa",
                    "description", "Mit frischen Champignons, Plockwurst, Schinken und Peperoni. With fresh Mushrooms, Sausage, Ham and Peppers",
                    "price", "€18.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "18",
                    "category", "PIZZA",
                    "name", "77. Veneto",
                    "description", "Mit Bresaola und Rucola und Parmesan Käse. With Smoked Beef, Rocket Salad and Parmesan Cheese",
                    "price", "€20.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // MANZO-RIND / BEEF
            Map.of(
                    "id", "19",
                    "category", "MANZO-RIND / BEEF",
                    "name", "112. Bistecca al Griglia",
                    "description", "Rumpsteak 220g mit Kräuterbutter und Pommes. Beef Filet 220g with herbed Butter and French Fries",
                    "price", "€32.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "20",
                    "category", "MANZO-RIND / BEEF",
                    "name", "113. Bistecca ai Cantarelli",
                    "description", "Rumpsteak 220g mit Pfifferlingen und Pommes. Beef Filet 220g with Chanterelles and French",
                    "price", "€35.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "21",
                    "category", "MANZO-RIND / BEEF",
                    "name", "613. Argentinisches Entrecote",
                    "description", "Argentinisches Entrecote vom Grill 220g auf Rucola mit Pommes. Argentinian Grilled Entrecote 220g on Rocket with Frensh Fries",
                    "price", "€31.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // POLLO-HÄNCHEN / CHICKEN
            Map.of(
                    "id", "22",
                    "category", "POLLO-HÄNCHEN / CHICKEN",
                    "name", "90. Pollo alla Griglia",
                    "description", "Griglia Hähnchenbrust vom Grill dazu Pommes Frites. Grilled Chicken Breast Filet with French Fries",
                    "price", "€24.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "23",
                    "category", "POLLO-HÄNCHEN / CHICKEN",
                    "name", "91. Pollo Funghi",
                    "description", "Hähnchenbrustfilet in Champignon sauce, dazu Pommes Frites. Chicken Breast Filet in Mushroom Sauce with French Fries",
                    "price", "€25.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "24",
                    "category", "POLLO-HÄNCHEN / CHICKEN",
                    "name", "92. Pollo al Pepe",
                    "description", "Hähnchenbrustfilet mit Pfefferrahm-Sauce dazu Pommes Frites. Chicken Breast Filet with Pepper-Cream Sauce and French fries",
                    "price", "€25.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // PESCE-FISCH / FISH
            Map.of(
                    "id", "25",
                    "category", "PESCE-FISCH / FISH",
                    "name", "120. Scampi Griglia",
                    "description", "Frische gegrillte Garnelen mit Zitrone und gemischten Salat. Fresh grilled Prawns with Lemon and fresh Salad",
                    "price", "€34.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "26",
                    "category", "PESCE-FISCH / FISH",
                    "name", "122. Salmone Griglia",
                    "description", "Frischer Lachs 200g vom Grill. Fresh grilled Salmon",
                    "price", "€27.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "27",
                    "category", "PESCE-FISCH / FISH",
                    "name", "123. Salmone Cardinale",
                    "description", "Frischer Lachs 200g mit Krabben in Hummer Sauce. Fresh Salmon with Prawns in Lobster Sauce",
                    "price", "€30.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // BURGERS
            Map.of(
                    "id", "28",
                    "category", "BURGERS",
                    "name", "124. Steak Burger",
                    "description", "Zartes Rindersteak (120 g) mit würzigem Käse, Zwiebeln, frischen Tomaten, knackigem Bionda-Salat, fein eingelegten Gurken und hausgemachter Spezial-Sauce. Tender Beef Steak (120 g), with cheese, onions, fresh tomatoes, Bionda Lettuce, Pickles and homemade Special-Sauce",
                    "sides", List.of("Pommes frites/French fries", "Süßkartoffel-Pommes/Sweet potato fries"),
                    "price", "€26.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "29",
                    "category", "BURGERS",
                    "name", "125. Chicken Filet Burger",
                    "description", "Saftiges Hähnchenbrustfilet mit Käse, Zwiebeln, Tomaten, Bionda-Salat, Gewürzgurken und hausgemachter Spezial-Sauce. Juicy Chicken Breast with Cheese, Onions, Tomatoes, Bionda Lettuce, Pickles and homemade Special-Sauce",
                    "sides", List.of("Pommes frites/French fries", "Süßkartoffel-Pommes/Sweet potato fries"),
                    "price", "€22.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "30",
                    "category", "BURGERS",
                    "name", "126. Fish Filet Burger",
                    "description", "Fischfilet mit würzigem Käse, frischen Zwiebeln und Tomaten, knackigem Bionda-Salat, fein eingelegten Gurken und hausgemachter Spezial-Sauce. Fish Fillet with flavorful Cheese, fresh Onions and Tomatoes, crisp Bionda Lettuce, Pickles and homemade Special-Sauce",
                    "sides", List.of("Pommes frites/French fries", "Süßkartoffel-Pommes/Sweet potato fries"),
                    "price", "€20.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // DESSERT
            Map.of(
                    "id", "31",
                    "category", "DESSERT",
                    "name", "Tiramisu",
                    "description", "Traditional Italian dessert",
                    "price", "€8.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "32",
                    "category", "DESSERT",
                    "name", "Creme Caramel",
                    "description", "Classic French dessert",
                    "price", "€8.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "33",
                    "category", "DESSERT",
                    "name", "Panna Cotta",
                    "description", "Italian cream dessert",
                    "price", "€8.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // SOFT DRINKS
            Map.of(
                    "id", "34",
                    "category", "SOFT DRINKS",
                    "name", "240. Coca Cola 0,33l",
                    "description", "Classic Coca Cola",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "35",
                    "category", "SOFT DRINKS",
                    "name", "242. Coca Cola Zero 0,33l",
                    "description", "Sugar-free Coca Cola",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "36",
                    "category", "SOFT DRINKS",
                    "name", "244. Fanta 0,33l",
                    "description", "Orange flavored soft drink",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "37",
                    "category", "SOFT DRINKS",
                    "name", "246. Spezi 0,33l",
                    "description", "Cola and orange mix",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "38",
                    "category", "SOFT DRINKS",
                    "name", "248. Sprite 0,33l",
                    "description", "Lemon-lime flavored soft drink",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "39",
                    "category", "SOFT DRINKS",
                    "name", "250. Ginger Ale 0,2l",
                    "description", "Ginger flavored carbonated drink",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "40",
                    "category", "SOFT DRINKS",
                    "name", "252. Bitter Lemon 0,2l",
                    "description", "Bitter lemon flavored drink",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "41",
                    "category", "SOFT DRINKS",
                    "name", "254. Tonic Water 0,2l",
                    "description", "Carbonated tonic water",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "42",
                    "category", "SOFT DRINKS",
                    "name", "258. Tafelwasser 0,33l",
                    "description", "Table water",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "43",
                    "category", "SOFT DRINKS",
                    "name", "260. San Pellegrino 0,75fl",
                    "description", "Italian sparkling water",
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "44",
                    "category", "SOFT DRINKS",
                    "name", "261. Acqua Panna 0,75fl",
                    "description", "Italian still water",
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // SÄFTE / JUICES
            Map.of(
                    "id", "45",
                    "category", "SÄFTE / JUICES",
                    "name", "224. Kirschsaft-Cherry-Juice 0,33l",
                    "description", "Cherry juice",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "46",
                    "category", "SÄFTE / JUICES",
                    "name", "226. Apfelsaft-Apple-Juice 0,33l",
                    "description", "Apple juice",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "47",
                    "category", "SÄFTE / JUICES",
                    "name", "228. Apfelschorle-Apple-Spritzer 0,33l",
                    "description", "Apple juice with sparkling water",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "48",
                    "category", "SÄFTE / JUICES",
                    "name", "220. Orangensaft-Orange-Juice 0,33l",
                    "description", "Orange juice",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "49",
                    "category", "SÄFTE / JUICES",
                    "name", "222. Bananensaft-Banana-Juice 0,33l",
                    "description", "Banana juice",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // HEIßE GETRÄNKE / HOT DRINKS
            Map.of(
                    "id", "50",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "201. Espresso",
                    "description", "Strong Italian coffee",
                    "price", "€3.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "51",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "202. Espresso Doppio",
                    "description", "Double espresso",
                    "price", "€5.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "52",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "203. Espresso Macchiato",
                    "description", "Espresso with a spot of milk",
                    "price", "€3.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "53",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "204. Cappucino",
                    "description", "Espresso with steamed milk and foam",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "54",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "205. Milchkaffe-Milk Coffee",
                    "description", "Coffee with milk",
                    "price", "€5.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "55",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "206. Latte Macchiato",
                    "description", "Layered milk coffee",
                    "price", "€5.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "56",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "207 Irish Coffee",
                    "description", "Coffee with Irish whiskey",
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "57",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "208. Kaffee-Coffee",
                    "description", "Regular coffee",
                    "price", "€3.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "58",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "209. Tee-Tea (verschiedene Sorten-different varieties)",
                    "description", "Various tea varieties",
                    "price", "€3.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "59",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "210. Schokolade mit Sahne-Chocolate with Cream",
                    "description", "Hot chocolate with cream",
                    "price", "€5.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "60",
                    "category", "HEIßE GETRÄNKE / HOT DRINKS",
                    "name", "211. Frische Minze-Fresh Mint",
                    "description", "Fresh mint tea",
                    "price", "€6.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // BIER / BEER
            Map.of(
                    "id", "61",
                    "category", "BIER / BEER",
                    "name", "271 Bitburger Premium Pils 0,4l-0,5l",
                    "description", "German premium pilsner",
                    "price", "€5.00/€6.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "62",
                    "category", "BIER / BEER",
                    "name", "272 Herrnbräu Premium Pils 0,4l-0,5l",
                    "description", "Premium pilsner beer",
                    "price", "€5.00/€6.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "63",
                    "category", "BIER / BEER",
                    "name", "273 Hefeweizen 0,5l",
                    "description", "Wheat beer",
                    "price", "€6.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "64",
                    "category", "BIER / BEER",
                    "name", "274 Hefeweizen Kristall 0,5l",
                    "description", "Clear wheat beer",
                    "price", "€6.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "65",
                    "category", "BIER / BEER",
                    "name", "275 Berliner weiße -(mit schuss,rot oder grün)",
                    "description", "Berlin wheat beer with syrup (red or green)",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "66",
                    "category", "BIER / BEER",
                    "name", "276 Bitburger alkoholfrei",
                    "description", "Non-alcoholic beer",
                    "price", "€3.90",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "67",
                    "category", "BIER / BEER",
                    "name", "277 Alsterwasser 0,4l (Biermixgetränk mixed beer)",
                    "description", "Beer mixed with lemonade",
                    "price", "€4.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "68",
                    "category", "BIER / BEER",
                    "name", "278 Alsterwasser 0,5l(Biermixgetränk mixed beer)",
                    "description", "Beer mixed with lemonade",
                    "price", "€5.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "69",
                    "category", "BIER / BEER",
                    "name", "279 Malztrunk 0,33l",
                    "description", "Malt drink",
                    "price", "€4.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "70",
                    "category", "BIER / BEER",
                    "name", "Bier 1,0l",
                    "description", "Large beer",
                    "price", "€11.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),
            // LONG DRINKS
            Map.of(
                    "id", "71",
                    "category", "LONG DRINKS",
                    "name", "Aperol Spritz",
                    "description", "Italian aperitif with Aperol, Prosecco and soda",
                    "price", "€10.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "72",
                    "category", "LONG DRINKS",
                    "name", "CampariSoda-Orange",
                    "description", "Campari with soda or orange juice",
                    "price", "€10.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "73",
                    "category", "LONG DRINKS",
                    "name", "Bacardi Cola",
                    "description", "Rum and cola cocktail",
                    "price", "€10.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "74",
                    "category", "LONG DRINKS",
                    "name", "Wodka Lemon",
                    "description", "Vodka with lemon mixer",
                    "price", "€10.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "75",
                    "category", "LONG DRINKS",
                    "name", "Gin Tonic",
                    "description", "Classic gin and tonic cocktail",
                    "price", "€10.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "76",
                    "category", "LONG DRINKS",
                    "name", "Limoncello Spritz",
                    "description", "Italian lemon liqueur spritz",
                    "price", "€10.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "77",
                    "category", "LONG DRINKS",
                    "name", "Hugo",
                    "description", "Prosecco with elderflower, mint and lime",
                    "price", "€10.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // WEISSWEINE / WHITE WINES
            Map.of(
                    "id", "78",
                    "category", "WEISSWEINE / WHITE WINES",
                    "name", "Pinot Grigio Trocken,Dry",
                    "description", "Dry Italian white wine",
                    "sides", List.of("Glas / Glass - 0,2l", "Karaffe / Carafe - 0,5l"),
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "79",
                    "category", "WEISSWEINE / WHITE WINES",
                    "name", "Soave Dry Trocken",
                    "description", "Dry Italian white wine from Veneto",
                    "sides", List.of("Glas / Glass - 0,2l", "Karaffe / Carafe - 0,5l"),
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "80",
                    "category", "WEISSWEINE / WHITE WINES",
                    "name", "Chardonnay Trocken,Dry",
                    "description", "Dry Chardonnay white wine",
                    "sides", List.of("Glas / Glass - 0,2l", "Karaffe / Carafe - 0,5l"),
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "81",
                    "category", "WEISSWEINE / WHITE WINES",
                    "name", "Weißweinschorle",
                    "description", "White wine spritzer with sparkling water",
                    "sides", List.of("Glas / Glass - 0,2l", "Karaffe / Carafe - 0,5l"),
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // ROT WEINE / RED WINES
            Map.of(
                    "id", "82",
                    "category", "ROT WEINE / RED WINES",
                    "name", "Chianti Trocken,Dry",
                    "description", "Dry Italian red wine from Tuscany",
                    "sides", List.of("Glas / Glass - 0,2l", "Karaffe / Carafe - 0,5l"),
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "83",
                    "category", "ROT WEINE / RED WINES",
                    "name", "Valpolicella Trocken,Dry",
                    "description", "Dry Italian red wine from Veneto",
                    "sides", List.of("Glas / Glass - 0,2l", "Karaffe / Carafe - 0,5l"),
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "84",
                    "category", "ROT WEINE / RED WINES",
                    "name", "Montepluciano Trocken,Dry",
                    "description", "Dry Italian red wine from Abruzzo",
                    "sides", List.of("Glas / Glass - 0,2l", "Karaffe / Carafe - 0,5l"),
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "85",
                    "category", "ROT WEINE / RED WINES",
                    "name", "Lambrusco Lieblich",
                    "description", "Semi-sweet Italian sparkling red wine",
                    "sides", List.of("Glas / Glass - 0,2l", "Karaffe / Carafe - 0,5l"),
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // ROSEWEIN / ROSE WINES
            Map.of(
                    "id", "86",
                    "category", "ROSEWEIN / ROSE WINES",
                    "name", "Rosatto Rose (italien)",
                    "description", "Italian rosé wine",
                    "sides", List.of("Glas / Glass - 0,2l", "Karaffe / Carafe - 0,5l"),
                    "price", "€7.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // WEINE AUS ITALIEN / WINE FROM ITALY
            Map.of(
                    "id", "87",
                    "category", "WEINE AUS ITALIEN / WINE FROM ITALY",
                    "name", "Chardonnay Trocken,Dry Fl 0,75l",
                    "description", "Dry Italian Chardonnay - full bottle",
                    "price", "€35.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "88",
                    "category", "WEINE AUS ITALIEN / WINE FROM ITALY",
                    "name", "Pinot Grigio Trocken,Dry. Fl 0,75l",
                    "description", "Dry Italian Pinot Grigio - full bottle",
                    "price", "€45.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "89",
                    "category", "WEINE AUS ITALIEN / WINE FROM ITALY",
                    "name", "Gavi Di Gavi Trocken,Dry Fl 0,75l",
                    "description", "Premium dry Italian white wine - full bottle",
                    "price", "€50.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            // ROTWEINE AUS ITALY / RED WINES FROM ITALY
            Map.of(
                    "id", "90",
                    "category", "ROTWEINE AUS ITALY / RED WINES FROM ITALY",
                    "name", "Nero 'Davola Sizilien, Trocken,Dry Fl 0,75l",
                    "description", "Dry Sicilian red wine - full bottle",
                    "price", "€47.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "91",
                    "category", "ROTWEINE AUS ITALY / RED WINES FROM ITALY",
                    "name", "Bardolino Trocken,Dry Fl 0,75l",
                    "description", "Dry Italian red wine from Lake Garda - full bottle",
                    "price", "€40.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "92",
                    "category", "ROTWEINE AUS ITALY / RED WINES FROM ITALY",
                    "name", "Merlot Trocken,Dry Fl 0,75l",
                    "description", "Dry Italian Merlot - full bottle",
                    "price", "€42.50",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "93",
                    "category", "ROTWEINE AUS ITALY / RED WINES FROM ITALY",
                    "name", "Chianti Classico Trocken,Dry Fl 0,75l",
                    "description", "Premium dry Chianti Classico - full bottle",
                    "price", "€50.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "94",
                    "category", "ROTWEINE AUS ITALY / RED WINES FROM ITALY",
                    "name", "Chianti Riserva Trocken,Dry Fl 0,75l",
                    "description", "Premium aged Chianti Riserva - full bottle",
                    "price", "€58.00",
                    "imageUrl", "https://via.placeholder.com/150"
            ),

            Map.of(
                    "id", "95",
                    "category", "ROTWEINE AUS ITALY / RED WINES FROM ITALY",
                    "name", "Barolo Trocken,Dry Fl 0,75l",
                    "description", "Premium Italian red wine from Piedmont - full bottle",
                    "price", "€48.00",
                    "imageUrl", "https://via.placeholder.com/150"
            )
    );

    return menuItems.stream()
            .map(item -> {
                    String name = (String) item.get("name");
                    String updatedImageUrl = Util.getImageUrl(name);

                    // Create a new map with all existing entries and override imageUrl
                    Map<String, Object> updatedItem = new HashMap<>(item);
                    updatedItem.put("imageUrl", updatedImageUrl);
                    return updatedItem;
            })
            .collect(Collectors.toList());
    }

    private static String getImageUrl(String name) {
            String convertedName = name.replace("ü", "u%CC%88")
                    .replace("ö", "o%CC%88")
                    .replace("ä", "a%CC%88")
                    .replace("ß", "%C3%9F")
                    .replace(" ", "+")
                    .replace(",", "%2C");
            return BASE_URL + convertedName + ".jpg";
    }

}
