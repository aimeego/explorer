## build

```bash
# this project template is generated by vue-cli
# > vue init webpack xxx
# index.html was pulled into 'src' folder, modified webpack config accordingly

# 1. install lastest node.js so you can execute 'npm' command
# npm is shipped with node.js
# here are two methods, you can choose either one

# 1.1. download zip or installer, OR
https://nodejs.org/en/download/
# 1.2. via package manager
https://nodejs.org/en/download/package-manager/

# 2. download explorer code
git clone https://github.com/nebulasio/explorer.git

# 3. install dependency
cd explorer/explorer-front
npm i

# 4. run webpack dev build on localhost
npm run dev

# 5. open http://localhost:8080/ in browser
# config port number in explorer-front/config/index.js!dev.port

# run webpack production build, results will output to 'dist' folder
npm run build
```