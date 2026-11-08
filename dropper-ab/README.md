# Dropper A/B — Fabric 1.21.4 (client-side)

## Derleme
1. Java 21 (JDK) kur.
2. Bu klasörde terminal aç:
   - Windows: `gradlew.bat build`
   - Mac/Linux: `./gradlew build`
3. Çıkan dosya: `build/libs/dropper-ab-1.0.0.jar`
4. Bu jar'ı ve Fabric API'yi (1.21.4) `.minecraft/mods` klasörüne at.

Not: `gradle.properties` içindeki `fabric_version` / `yarn_mappings` sürümleri
https://fabricmc.net/develop adresindeki 1.21.4 değerleriyle değiştirilebilir.

## Kullanım
1. Kontroller'den "Sosyal Etkileşimler" tuşunu kaldır (tıkla, Esc) → artık **P** menüyü açar.
2. Menüde A ve B itemlerini ayarla (yaz, "Elimdeki → A/B" ya da "Son açtığım dropper'dan ata").
   A/B tuşlarını menüden değiştir (varsayılan G ve H).
3. A ve B'yi dropper'a koy, dropper'ı **bir kez aç** (sıralama kaydedilir).
4. Dropper'ın butonuna bakarken A ya da B tuşuna bas (buton otomatik basılır;
   kapalıysa 5 saniye içinde kendin bas).
5. Çıkan item yerde, uçarken, envanterde ve elinde seçtiğin item olarak görünür.
   Dropper'a bakınca seçtiğin item yok, diğeri kendi yuvasında görünür.
6. Itemi dropper'a geri koyunca ikisi de ilk sırasıyla görünür, illüzyon biter.

Her şey sadece senin ekranında. Sunucu ve diğer oyuncular gerçek itemi görür.
