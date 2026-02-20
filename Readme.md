# Getting Started
mvn spring-boot:run

# Building Solution
mvn clean package

# per spegnere il server su porta 8083
ps aux | grep "http.server 8083"
kill <PID>
(Opzionale) Rimuovi i file:

sudo rm -rf /opt/hicman-maintenance
sudo rm -f /var/log/hicman-maintenance.log

# Installing Docker
sudo apt-get update
sudo apt-get install ca-certificates curl
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc
# Add the repository to Apt sources:
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin -y

# Installing Docker-Compose
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version

# Nginx Start
docker-compose up -d
Email: admin@example.com 
PWD: changeme

# Creating Docker Image
docker build -t hicmancorporatesitev1 .

# Exporting Docker image
docker save -o C:\DigitalOceanHicman\HicmanCorporatesite.tar hicmancorporatesitev1

docker save -o /home/osc/Server/HicmanSite/exports/HicmanCorporatesitev.tar hicmancorporatesitev

# Trasporting into server
scp -i "chiavesrv.pem" srv:/home/ubuntu/

# Importing into SRV Docker
(sudo) docker load -i corporatesite.tar

# Running Image
sudo docker run -d -p 8080:8080 corporatesitev18

# Cose importanti
deve essere fatto da 3 pagine, Home, Chi siamo e contattaci.

Home deve essere composta da:
1) Video Showreel con bottone di invio
2) Buisness case in evidenza
3) Sotto siti
4) Contattaci/footer

Dimensioni immagini card: 

Business case in evidenza:
Dimensione ottimale: 1000x1000 pixel
Layout suggerito:
25% superiore: area per il titolo integrato nell'immagine
75% restante: area principale dell'immagine che sfuma verso il basso per l'overlay

Business units:
Dimensione ottimale: 800x1200 pixel
Layout suggerito:
20% superiore: area per il titolo integrato nell'immagine
80% restante: area principale dell'immagine che sfuma verso il basso per l'overlay con i punti elenco

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.1/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.1/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.1/reference/web/servlet.html)
* [Thymeleaf](https://docs.spring.io/spring-boot/3.4.1/reference/web/servlet.html#web.servlet.spring-mvc.template-engines)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/3.4.1/reference/using/devtools.html)
* [Spring Security](https://docs.spring.io/spring-boot/3.4.1/reference/web/spring-security.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.4.1/reference/data/sql.html#data.sql.jpa-and-spring-data)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Handling Form Submission](https://spring.io/guides/gs/handling-form-submission/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

