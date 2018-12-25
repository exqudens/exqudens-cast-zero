package exqudens.cast.test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import exqudens.cast.api.model.Item;
import exqudens.cast.api.model.Order;
import exqudens.cast.api.model.User;
import exqudens.cast.api.model.graph.Graph;
import exqudens.cast.api.util.Graphs;
import exqudens.cast.client.ExqudensCastClient;

public class Test1 {

    private static GenericContainer<?> container;

    @BeforeClass
    public static void beforeClass() {
        try {

            Path source = Paths.get("..", "exqudens-cast-zero-server", "build", "libs", "exqudens-cast-zero-server-1.0.0-component.jar");
            Path target = Paths.get("tmp", "exqudens-cast-zero-server-1.0.0-component.jar");
            Files.copy(source, target);

            container = new GenericContainer<>("openjdk:8");
            container.withStartupAttempts(1);
            container.withMinimumRunningDuration(Duration.ofSeconds(30));
            //container.copyFileToContainer(transferable, containerPath);
            //container.withCopyFileToContainer(MountableFile.forHostPath(Paths.get("..", "exqudens-cast-zero-server", "build", "libs", "exqudens-cast-zero-server-1.0.0-component.jar")), "/usr/src/myapp/");
            container.withCommand("/usr/bin/java", "-jar", "/usr/src/myapp/exqudens-cast-zero-server-1.0.0-component.jar");
            container.withExposedPorts(8080);
            container.waitingFor(Wait.defaultWaitStrategy());
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
    public void test97() {
        try {
            ExecResult execResult = container.execInContainer("ls", "/usr/src/myapp");
            System.out.println(execResult.getStdout());
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Ignore
    @Test
    public void test98() {
        try {
            System.out.println("---");
            Stream.of(Paths.get("..", "exqudens-cast-zero-server", "build", "libs").toFile().listFiles()).map(file -> file.getAbsolutePath() + ": " + file.lastModified()).forEach(System.out::println);
            System.out.println("---");
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Ignore
    @Test
    public void test99() {
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

            ExqudensCastClient client = ExqudensCastClient.newInstance("http://localhost", 8080, "", 10000);
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
