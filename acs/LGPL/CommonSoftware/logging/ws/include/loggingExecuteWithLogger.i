template <class F, class... T>
auto executeWithLogger(std::string tname, F f, T... args) -> typename std::result_of<F(T...)>::type {
    std::unique_ptr<LoggingProxy> logger{new LoggingProxy(0, 0, 31)};
    LoggingProxy::init(logger.get());
    LoggingProxy::ThreadName(tname.c_str());
    std::function<typename std::result_of<F(T...)>::type (T...)> func = f;
    return func(std::forward<T>(args)...);
}
