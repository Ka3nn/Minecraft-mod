# Dropper Swap (Fabric 1.21.4, sadece istemci)

## Derleme
1. https://fabricmc.net/develop/template/ adresinden Minecraft 1.21.4 için boş bir mod şablonu indir
   (içinde gradlew ve gradle klasörü hazır gelir).
2. Şablonun `src` klasörünü sil, bu projedeki `src`, `build.gradle`, `gradle.properties`, `settings.gradle` dosyalarını üstüne kopyala.
   (Şablondaki gradle.properties sürümleri daha yeniyse onları kullan.)
3. Java 21 kurulu olsun ve `gradlew build` çalıştır (Windows: `gradlew.bat build`).
4. `build/libs/dropperswap-1.0.0.jar` dosyasını Fabric API ile birlikte `.minecraft/mods` klasörüne at.

## Kullanım
- Ayarlar > Kontroller > Tuş Atamaları > "Sosyal Etkileşimler" tuşunu kaldır. Sonra **P** menüyü açar
  (menü tuşu da "Dropper Swap" kategorisinden değiştirilebilir).
- Menüde A ve B itemlerini yaz (örn. `minecraft:diamond`) ya da elindekini "Eldeki" ile seç, **Kaydet**.
- Dropper'ı ateşlemeden önce **A** (varsayılan Num 1) veya **B** (varsayılan Num 2) tuşuna bas.
- Çıkan item seçtiğinden farklıysa sadece senin ekranında:
  havadaki item, envanterindeki item ve item adı seçtiğin item olarak görünür;
  dropper'da kalan item ise diğer item olarak görünür.
- Itemi dropper'a geri koyup dropper ekranını kapatınca görüntü kendiliğinden normale döner.
- **Görüntüyü sıfırla**: takası hemen kapatır. **Her şeyi sıfırla**: A/B seçimlerini de temizler.

## Bilinmesi gerekenler
- Tamamen görsel ve istemci tarafıdır; sunucuya hiçbir şey göndermez. Diğer oyuncular gerçek itemi görür.
- Takas açıkken envanterindeki BÜTÜN A ve B itemleri yer değiştirmiş görünür.
  Tur sırasında envanterinde başka A/B itemi tutma.
- A/B tuşları sohbet ya da bir menü açıkken çalışmaz; dropper'ı ateşlemeden önce bas.
- Dropper'da her itemden birer tane olduğu varsayılır.
