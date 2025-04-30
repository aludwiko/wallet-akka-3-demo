# wallet


To understand the Akka concepts that are the basis for this example, see [Development Process](https://doc.akka.io/concepts/development-process.html) in the documentation.


This project contains the skeleton to create an Akka service. To understand more about these components, see [Developing services](https://doc.akka.io/java/index.html). Examples can be found [here](https://doc.akka.io/java/samples.html).


Use Maven to build your project:

```shell
mvn compile
```


When running an Akka service locally.

To start your service locally, run:

```shell
mvn compile exec:java
```

This command will start your Akka service. With your Akka service running, the endpoint it's available at:

## create wallet a

```shell
curl -i https://odd-union-7324.gcp-us-east1.akka.services/wallets/a/create \
  -X POST \
  --header "Content-Type: application/json" \
  --data '{"ownerId": "o1", "currency": "PLN"}'
```

## deposit funds a

```shell
curl -i https://odd-union-7324.gcp-us-east1.akka.services/wallets/a/deposit \
  -X POST \
  --header "Content-Type: application/json" \
  --data '{"ownerId": "o1", "currency": "PLN", "amount": "100"}'
```

## withdraw funds a

```shell
curl -i  https://odd-union-7324.gcp-us-east1.akka.services/wallets/a/withdraw \
  -X POST \
  --header "Content-Type: application/json" \
  --data '{"ownerId": "o1", "currency": "PLN", "amount": "10"}'
```

## create wallet b

```shell
curl -i https://odd-union-7324.gcp-us-east1.akka.services/wallets/b/create \
  -X POST \
  --header "Content-Type: application/json" \
  --data '{"ownerId": "o1", "currency": "PLN"}'
```

## deposit funds b

```shell
curl -i https://odd-union-7324.gcp-us-east1.akka.services/wallets/b/deposit \
  -X POST \
  --header "Content-Type: application/json" \
  --data '{"ownerId": "o1", "currency": "PLN", "amount": "100"}'
```

## transfer 30 from a to b

```shell
curl -i https://odd-union-7324.gcp-us-east1.akka.services/wallets/a/transfer \
  -X POST \
  --header "Content-Type: application/json" \
  --data '{"ownerId": "o1", "currency": "PLN", "amount": "30", "destinationWalletId": "b"}'
```

## get wallet a

```shell
curl https://odd-union-7324.gcp-us-east1.akka.services/wallets/a
```

## get all wallets

```shell
curl https://odd-union-7324.gcp-us-east1.akka.services/wallets
```


You can use the [Akka Console](https://console.akka.io) to create a project and see the status of your service.

Build container image:

```shell
mvn clean install -DskipTests
```

Install the `akka` CLI as documented in [Install Akka CLI](https://doc.akka.io/reference/cli/index.html).

Deploy the service using the image tag from above `mvn install`:

```shell
akka service deploy wallet wallet:tag-name --push
```

Refer to [Deploy and manage services](https://doc.akka.io/operations/services/deploy-service.html)
for more information.
