package exqudens.cast.test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import exqudens.cast.api.model.Item;
import exqudens.cast.api.model.Order;
import exqudens.cast.api.model.User;
import exqudens.cast.api.model.graph.Graph;
import exqudens.cast.api.util.Graphs;
import exqudens.cast.client.ExqudensCastClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.StartupCheckStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder;
import org.testcontainers.shaded.okhttp3.OkHttpClient;
import org.testcontainers.shaded.okhttp3.Request;
import org.testcontainers.shaded.okhttp3.Response;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Test1 {

    private static GenericContainer<?> container;

    @BeforeClass
    public static void beforeClass() {
        try {

            Path path = Paths.get("..", "exqudens-cast-zero-server", "build", "libs", "exqudens-cast-zero-server-1.0.0-component.jar");
            Consumer<DockerfileBuilder> builderConsumer = builder -> {
                builder
                        .from("openjdk:8")
                        .copy(
                                path.toFile().getAbsolutePath(),
                                "/usr/src/myapp/" + path.toFile().getName()
                        )
                        .cmd("/usr/bin/java", "-jar", "/usr/src/myapp/" + path.toFile().getName())
                        .build();
            };
            ImageFromDockerfile image = new ImageFromDockerfile()
                    .withDockerfileFromBuilder(builderConsumer)
                    .withFileFromPath(path.toFile().getAbsolutePath(), path);
            container = new GenericContainer<>(image);
            container.withLabel("name", "app");
            container.withStartupAttempts(1);
            container.withMinimumRunningDuration(Duration.ofSeconds(30));
            container.withExposedPorts(8080);
            container.withStartupCheckStrategy(new StartupCheckStrategy() {
                @Override
                public StartupStatus checkStartupState(DockerClient dockerClient, String containerId) {
                    try {
                        List<Container> containers = dockerClient.listContainersCmd().exec();
                        for (Container c : containers) {
                            boolean present = c
                                    .getLabels()
                                    .entrySet()
                                    .stream()
                                    .filter(entry -> "name".equals(entry.getKey()))
                                    .map(Entry::getValue)
                                    .filter(name -> "app".equals(name))
                                    .findFirst()
                                    .isPresent();
                            if (present) {
                                OkHttpClient client = new OkHttpClient();
                                Response response = client.newCall(new Request.Builder().url("http://localhost:" + c.ports[0].getPublicPort() + "/").build()).execute();
                                if (response.code() == 200) {
                                    return StartupStatus.SUCCESSFUL;
                                }
                            }
                        }
                        return StartupStatus.FAILED;
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            container.start();

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void afterClass() {
        container.stop();
        container.close();
        container = null;
    }

    @Test
    public void test1() {
        try {

            List<User> ethalonUsers = new ArrayList<>();
            List<Order> ethalonOrders = new ArrayList<>();
            List<Item> ethalonItems = new ArrayList<>();

            ethalonUsers.add(new User("name_1", null));
            ethalonOrders.add(new Order("number_1", null, null));
            ethalonOrders.add(new Order("number_2", null, null));
            ethalonItems.add(new Item("description_1", null));
            ethalonItems.add(new Item("description_2", null));
            ethalonItems.add(new Item("description_3", null));

            ethalonUsers.get(0).setOrders(ethalonOrders);
            ethalonOrders.get(0).setItems(ethalonItems.subList(0, 2));
            ethalonOrders.get(1).setItems(ethalonItems.subList(2, 3));

            ethalonOrders.stream().forEach(order -> order.setUser(ethalonUsers.get(0)));
            ethalonItems.subList(0, 2).stream().forEach(item -> item.setOrder(ethalonOrders.get(0)));
            ethalonItems.subList(2, 3).stream().forEach(item -> item.setOrder(ethalonOrders.get(1)));

            Graph graph = Graphs.listToGraph(
                Object.class,
                ethalonOrders.stream().map(Object.class::cast).collect(Collectors.toList()),
                System::identityHashCode,
                Arrays.asList(User.class, Order.class, Item.class),
                null,
                null
            );

            ExqudensCastClient client = ExqudensCastClient.newInstance("http://localhost", container.getMappedPort(8080), "", 10000);
            graph = client.apiGraphOrder(graph);

            List<Order> resultOrders = Graphs.graphToList(graph, Order.class);

            System.out.println(ethalonOrders.get(1).getItems().size());
            System.out.println(resultOrders.get(1).getItems().size());

        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
