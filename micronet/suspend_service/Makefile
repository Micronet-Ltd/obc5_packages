LDFLAGS=-pthread -lpthread
CUSTOMDEFINES = LINUX_BUILD
CFLAGS= -pthread -DUSE_THREADS -Wall -g -O0 -D$(CUSTOMDEFINES)

OBJECTS := AWakeLock.o suspend_service.o
TARGET := suspend_service



$(TARGET): $(OBJECTS)


clean:
	-rm $(OBJECTS) $(TARGET)
