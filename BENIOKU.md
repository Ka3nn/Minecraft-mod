# Dropper Swap (Fabric 1.21.4, istemci tarafı, Türkçe)

## Önemli
Sunucularda (DonutSMP vb.) dropper'dan hangi itemin çıkacağını SUNUCU rastgele belirler; hiçbir istemci
modu bunu değiştiremez. Bu mod, tuşa atanan itemin çıktığını **sadece senin ekranında** gösterir.
Diğer oyuncular gerçek sonucu görür.

## Derleme
1. https://fabricmc.net/develop/template/ adresinden Minecraft 1.21.4 için boş mod şablonu indir.
2. Şablonun `src` klasörünü sil; bu projedeki `src`, `build.gradle`, `gradle.properties`, `settings.gradle` dosyalarını üstüne kopyala.
3. Java 21 ile `gradlew build` (Windows: `gradlew.bat build`).
4. `build/libs/dropperswap-1.0.0.jar` dosyasını Fabric API ile birlikte `.minecraft/mods` klasörüne at.

## Tuşlar (Ayarlar > Kontroller > Tuş Atamaları > "Dropper Swap")
| Tuş | Varsayılan | Görev |
|---|---|---|
| A itemini ata | Num 1 | Lever çevrilince A çıkmış gibi görünür (menü açılmaz) |
| B itemini ata | Num 2 | Lever çevrilince B çıkmış gibi görünür (menü açılmaz) |
| Atanan itemi sıfırla | boş, sen ata | Atanan itemi kaldırır, görüntü normale döner |
| Mod menüsünü aç | P | "Sosyal Etkileşimler" tuşunu kaldırınca P menüyü açar |

## A ve B'yi ayarlamak (yazı yazmadan)
- **Otomatik:** Dropper'ı açman yeterli; içindeki 2 farklı item kendiliğinden A ve B olur.
- **Listeden seç:** Menüde oyundaki tüm itemler ızgara halinde çıkar, arayıp tıklayarak seçersin.
- **Eldeki:** Elinde tuttuğun item atanır.

## Menü seçenekleri
- Dropper'dan otomatik A/B al: Açık/Kapalı
- Bildirim yazısı: Açık/Gizli (tuşa basınca çıkan "A seçildi" yazısını gizler)
- Seçim her lever'da geçerli: Açık/Kapalı
- Atanan itemi sıfırla, A ve B'yi temizle

## Kullanım
1. Dropper'a 2 farklı item koy (birer tane), lever bağla, dropper'ı bir kere aç-kapat (A/B otomatik atanır).
2. A veya B tuşuna bas, lever'ı çevir.
3. Itemi dropper'a geri koyup ekranı kapatınca bir sonraki lever'a hazır olur.

Not: Takas açıkken envanterindeki bütün A ve B itemleri yer değiştirmiş görünür; envanterde başka A/B tutma.
