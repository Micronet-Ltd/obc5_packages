LDFLAGS=-lpthread
CFLAGS=-std=c99 -pthread -DUSE_THREADS -Wall -Werror

OBJECTS := main.o frame.o
TARGET := main



$(TARGET): $(OBJECTS)


clean:
	-rm $(OBJECTS) $(TARGET)
