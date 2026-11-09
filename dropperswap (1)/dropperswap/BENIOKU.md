# Dropper Swap (Fabric 1.21.4, sadece istemci, Türkçe)

## Derleme
1. https://fabricmc.net/develop/template/ adresinden Minecraft 1.21.4 için boş bir mod şablonu indir.
2. Şablonun `src` klasörünü sil; bu projedeki `src`, `build.gradle`, `gradle.properties`, `settings.gradle` dosyalarını üstüne kopyala.
3. Java 21 ile `gradlew build` (Windows: `gradlew.bat build`) çalıştır.
4. `build/libs/dropperswap-1.0.0.jar` dosyasını Fabric API ile birlikte `.minecraft/mods` klasörüne at.

## Kurulum (oyunda)
- Dropper'a 2 farklı item koy (her birinden 1 tane), dropper'a bir lever bağla.
- Ayarlar > Kontroller > Tuş Atamaları > "Sosyal Etkileşimler" tuşunu **kaldır**. Artık **P** mod menüsünü açar.
  (Menü tuşu "Dropper Swap" kategorisinden değiştirilebilir.)
- Menüde A ve B itemlerini yaz (örn. `minecraft:diamond`) veya elindekini "Eldeki" ile seç, **Kaydet**.

## Kullanım
- **A** (Num 1) veya **B** (Num 2) tuşuna bas. Menü açılmaz, seçim arka planda yapılır.
- Lever'ı çevir. Dropper'dan hangisi çıkarsa çıksın, senin ekranında seçtiğin item çıkmış gibi görünür;
  dropper'da kalan item de diğeri olarak görünür.
- Itemi dropper'a geri koyup ekranı kapatınca görüntü gerçeğe döner ve bir sonraki lever çevirişine hazırlanır.

## Menü seçenekleri
- **Bildirim yazısı: Açık/Gizli**: A/B'ye basınca hotbar üstünde çıkan "A seçildi" yazısını gizler.
- **Seçim kalıcı (her lever)**: Açıkken bir kere A/B'ye basman yeter, her lever çevirişinde geçerli olur.
- **Görüntüyü sıfırla**: Takası ve seçimi hemen kapatır. **Her şeyi sıfırla**: A/B itemlerini de temizler.
- "Görüntüyü sıfırla" için ayrıca tuş da atanabilir (varsayılan boş).

## Bilinmesi gerekenler
- Tamamen görsel ve istemci tarafıdır; sunucuya hiçbir şey göndermez. Diğer oyuncular gerçeği görür.
- Takas açıkken envanterindeki BÜTÜN A ve B itemleri yer değiştirmiş görünür; envanterde başka A/B tutma.
- A/B tuşları sohbet veya bir menü açıkken çalışmaz.
