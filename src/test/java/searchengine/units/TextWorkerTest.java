package searchengine.units;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import searchengine.components.TextWorker;
import searchengine.config.SearchSettings;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("\"TextWorker\" unit tests")
class TextWorkerTest {
    String text = """
            Повторное появление леопарда в Осетии позволяет предположить,
            что леопард постоянно обитает в некоторых районах Северного
            Кавказа.
            """;
    String gibson = """
            Гоми.
                 Где кончается гоми  и  начинается мир? Уже лет сто назад японцам некуда
            было  сваливать гоми вокруг Токио,  так  они  придумали, как делать из  гоми
            жизненное пространство.  В  1969 году  японцы выстроили в  Токийском  заливе
            небольшой островок, целиком из  гоми, и назвали его Островом Мечты. Но город
            непрерывным потоком выбрасывал свои девять тысяч тонн гоми в день, и  вскоре
            они построили Новый Остров  Мечты,  а сегодня  технология отлажена,  и новые
            японские  территории растут в Тихом океане,  как грибы  после дождя. Об этом
            рассказывают   по  телевизору   в  новостях.  Рубин  смотрит,  но  никак  не
            комментирует.
                 Зачем ему говорить о гоми? Это его  среда обитания, воздух,  которым он
            дышит.  Всю свою жизнь он плавает в гоми как рыба в воде.  Рубин мотается по
            округе в  своем  грузовике-развалюхе,  переделанном из древнего аэродромного
            "мерседеса", крышу которого  закрывает огромный, перекачивающийся из стороны
            в сторону  полупустой баллон с  природным газом.  Он постоянно  что-то ищет,
            какие-то вещи под  чертежи, нацарапанные изнутри  на  его  веках кем-то, кто
            выполняет  у  него  роль Музы.  Рубин  тащит  в  дом  гоми. Иногда  гоми еще
            работает. Иногда, как в случае с Лайзой, дышит.
                 Я встретил Лайзу на  одной  из вечеринок у  Рубина. Он часто устраивает
            вечеринки.  Сам их не особенно любит, но вечеринки у него всегда классные. Я
            уже счет  потерял, сколько раз той осенью  просыпался на пенопластовой плите
            под  рев древней  автоматической кофеварки Рубина --  этакого  полированного
            монстра, на котором восседает огромный хромированный орел. Отражаясь от стен
            из гофрированного металла, звук превращается в жуткий рев,  но в то же время
            и  здорово  успокаивает.  Ревет  --   значит,   будет  кофе.  Значит,  жизнь
            продолжается.
            В первый раз я увидел ее в "кухонной зоне". Это не совсем кухня, просто
            три холодильника, плитка и  конвекторная  печка, которую он притащил в числе
            прочего гоми. Первый раз: она  стоит у открытого, "пивного", холодильника, а
            оттуда на нее падает  свет. Я  сразу заметил скулы, волевую складку  рта, но
            также  заметил черный  блеск  полиуглерода  у  запястья и блестящее пятно на
            руке,  где экзоскелет натер кожу. Я тогда был  слишком  пьян, чтобы все  это
            понять, но все же сообразил: что-то здесь не то.  И  я поступил  точь-в-точь
            так, как  люди  обычно  поступают  с Лайзой: переключился  "на другое кино".
            Вместо  пива  направился к винным  бутылкам, что стояли на стойке  у печи, и
            даже не оглянулся.
                 Но она сама меня нашла.
            """;

    private TextWorker worker;

    @BeforeEach
    void init()  {
        worker = new TextWorker(new SearchSettings());
    }

    public static Stream<Arguments> urlProvider() {
        return Stream.of(
                arguments("https://google.com", "/"),
                arguments("https://google.com/", "/"),
                arguments("https://google.com/minor/hello-kitty", "/minor/hello-kitty")
        );
    }

    @Test
    @DisplayName("Text to lemmas")
    void testLemmas() throws IOException {
        int size = 12;
        int numberOfLeopards = 2;
        String word = "леопард";

        worker.init();
        Map<String, Integer> lemmas = worker.lemmas(text);

        assertEquals(size, lemmas.size());
        assertEquals(numberOfLeopards, lemmas.get(word));
    }

    @ParameterizedTest
    @DisplayName("Cut url address when domain url \"https://google.com\"")
    @MethodSource("urlProvider")
    void testCutUrl(String url, String expected) {
        String domain = "https://google.com";
        assertEquals(expected, worker.path(url, domain));
    }

    @ParameterizedTest
    @DisplayName("Cut url address when domain url \"https://google.com/\"")
    @MethodSource("urlProvider")
    void testCutUrlSecondDomain(String url, String expected) {
        String domain = "https://google.com/";
        assertEquals(expected, worker.path(url, domain));
    }

    @Test
    @DisplayName("URL decode")
    void testUrlDecode() {
        String expected = "https://sendel.ru/books/";
        String url = "url=https%3A%2F%2Fsendel.ru%2Fbooks%2F";
        String result = worker.urlDecode(url);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("URL decode without head")
    void testUrlDecodeWithoutHead() {
        String expected = "https://sendel.ru/books/";
        String url = "url=sendel.ru%2Fbooks%2F";
        String result = worker.urlDecode(url);
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("Get snippets from the text")
    void testSnippets() {
        String expect = """
                ...но  никак  некомментирует.     Зачем ему говорить о гоми? Это его  среда обитания, воздух,  которым ондышит.  Всю свою жизнь он плавает в гоми как рыба в воде.  Рубин мотается поокруге в  своем  грузовике-развалюхе,  переделанном из древнего аэродромного"мерседеса", крышу которого  закрывает огромны...
                ...стья и блестящее пятно наруке,  где экзоскелет натер кожу. Я тогда был  слишком  пьян, чтобы все  этопонять, но все же сообразил: что-то здесь не то.  И  я поступил  точь-в-точьтак, как  люди  обычно  поступают  с Лайзой: переключился  "на другое кино".Вместо  пива  направился к винным  бутылкам, чт...
                """;
        String snippets = worker.snippets(gibson, "(воздух|экзоскелет|кошечка)");
        assertEquals(expect, snippets);
    }

    @Test
    @DisplayName("First char from word to upper case")
    void testFirstToUpperCase() {
        String word = "экзоскелет";
        String expect = "Экзоскелет";
        String result = worker.firstCharToUpperCase(word);
        assertEquals(expect, result);
    }

    @Test
    @DisplayName("Add bold tags")
    void testBold() {
        String word = "экзоскелет";
        String text = "блестящее пятно наруке, где экзоскелет натер кожу";
        String expect = "блестящее пятно наруке, где <b>экзоскелет</b> натер кожу";
        String result = worker.bold(word, text);
        assertEquals(expect, result);
    }
}