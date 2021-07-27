version=$(git describe --tags --always --dirty=-dirty)

clean_up () {
    echo "> clean up"
    rm -fr target/
}

get_name() {
    echo "imagej-timebar-${version}.jar"
}

package() {
    echo "> generate pom.xml from template (version: ${version})"
    mkdir --parents target
    sed -e "s#<version>X.Y.Z</version>#<version>${version}</version>#" template-pom.xml > pom.xml
    echo "> package with maven"
    mvn -B package
    echo "> cleanup pom.xml"
    rm pom.xml
    echo "> ok"
}

show_usage() {
    echo "Usage: build-script.sh [p|package] [c|clean] [n|name]"
}

if [ $# -eq 0 ]; then
    show_usage
fi

while [ $# -gt 0 ]; do
    key="$1"
    case $key in
        clean|c)
            clean_up 0
            shift
            ;;
        package|p)
            package
            shift
            ;;
        name|n)
            get_name
            shift
            ;;
        *)
            show_usage
            shift
            ;;
    esac
done
