# Semestrální práce z KIV/UPS
Project byl vytvořen pro splnění požadavků předmětu KIV/UPS. Jedná se o síťovou hru - je tedy vyžadován server a klient.
## Síťová hra lodě
- tématem semestrální práce je síťová hra lodě (námořní bitva)
### Server
Server je psán v jazyce C a jedná se o konzolovou aplikaci.
#### Build a spuštění
Pro překlad serveru se vyskytujeme v adresáři `/server/`.
Pro buildění je využíván nástroj CMake, který vytvoří příslušný Makefile.
Spustíme příkaz `cmake` a zvolíme výstup do složky `build`:
```
cmake -B ./build/
```
Přemístíme se do adresáře ´build/´:
```
cd build
```
Nyní Makefilu sestavíme spustitelný soubor:
```
make
```

Vytvoří se spustitelný soubor ```server```. Ten spustíme společně se zadaným portem, na kterým má server poslouchat:
```
./server --port 9123 
```
nebo
```
./server -port 9123 
```

Můžeme použít nepovinné parametry pro nastavení maxilního počtu hráčů a místností + úvést IP adresu
nebo
```
-r 15  | --rooms 15
```
```
-pl 30  | --players 15
```
```
-ip <ip adresa>
```

### Klient
Klient je napsán v jazyce Java a jedná se o okenní aplikaci využívající JavaFX.
#### Build a spuštění maven
Předpokládá se, že před zadáním následujících příkazů se nacházíme v adresáři `/client/`
Pro spuštění můžeme použít wrapper Gradle. Aplikaci pomocí něj spustíme zadáním příkazu:
```
./gradlew run
```
Nebo vygenerujeme jar nacházející se ve složce build/libs/client.jar
```
./gradlew jar
```


#### Build a spuštění maven
Předpokládá se, že před zadáním následujících příkazů se nacházíme v adresáři `/client/`
Pro přeložení je potřeba mít nainstalován maven - pro překlad programu se použije příkaz
```
mvn clean install
```
Ve vytvořené složce target, se nachází spustitelný soubor client-2-jar-with-dependencies.jar