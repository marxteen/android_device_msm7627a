if [ ! -f vendor/samsung/delos3geur/proprietary/app/app-debug.apk ]; then
mkdir -p vendor/samsung/delos3geur/proprietary/app
    wget https://github.com/Grace5921/OtaUpdater/releases/download/untagged-2d2485f3048081177c51/app-debug.apk -O vendor/samsung/delos3geur/proprietary/app/app-debug.apk
fi

if [ ! -f system/bt ]; then
rm -rf system/bt
fi

function delos3geur
{
lunch delos3geur-userdebug
make -j4 bacon
}
function delos3geur_select
{
lunch delos3geur-userdebug
}
