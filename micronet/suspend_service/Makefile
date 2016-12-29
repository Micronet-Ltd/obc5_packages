LDFLAGS=-pthread -lpthread
CUSTOMDEFINES = LINUX_BUILD
CFLAGS=-std=c99 -pthread -DUSE_THREADS -Wall -g -O0 -D$(CUSTOMDEFINES)

OBJECTS := suspend_service.o
TARGET := suspend_service



$(TARGET): $(OBJECTS)


clean:
	-rm $(OBJECTS) $(TARGET)
