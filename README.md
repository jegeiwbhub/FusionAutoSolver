# Fusen Auto Solver (Android)

यह एक Android Studio project है जो 9×9 block puzzle के लिए screen-reading + auto-drag solver का MVP देता है।

## इस्तेमाल
1. Project को Android Studio में खोलें और APK बनाएं।
2. फोन में APK install करें।
3. **Settings → Accessibility → Fusen Auto Solver** को ON करें।
4. Puzzle game खोलें।
5. स्क्रीन पर floating **AUTO** बटन दबाएं।

## जरूरी बातें
- Android 11 (API 30) या उससे ऊपर चाहिए।
- App screenshot को local device पर पढ़ता है; कोई server/network जरूरी नहीं है।
- यह version screenshot में दिख रहे 9×9 cyan-grid layout के लिए tuned है। दूसरे resolution/game layout में detection को adjust करना पड़ सकता है।
- पहली बार केवल एक चाल से test करें और जरूरत हो तो AUTO को तुरंत रोकने के लिए service को Accessibility Settings से OFF करें।

## Build
Android Studio → Open → इस folder को चुनें → Build APK.
