## Build steps for Lib Mode

### do not forget to initialize submodules.
`git submodule init --update`

### To build for library mode, generating the .aar file:

`./gradlew :main:assembleSkeletonOvpn2Release`

### File will be generated at the folder:

`main/build/outputs/aar`